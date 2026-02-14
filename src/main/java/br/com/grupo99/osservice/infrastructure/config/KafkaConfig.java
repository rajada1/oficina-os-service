package br.com.grupo99.osservice.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuração do Apache Kafka para arquitetura de eventos.
 * Inclui Dead Letter Topics e retry com backoff exponencial.
 */
@Slf4j
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:os-service-group}")
    private String groupId;

    // ==================== TOPIC NAMES ====================
    public static final String TOPIC_OS_EVENTS = "os-events";
    public static final String TOPIC_BILLING_EVENTS = "billing-events";
    public static final String TOPIC_EXECUTION_EVENTS = "execution-events";

    // Dead Letter Topics
    public static final String DLT_OS_EVENTS = "os-events.DLT";
    public static final String DLT_BILLING_EVENTS = "billing-events.DLT";
    public static final String DLT_EXECUTION_EVENTS = "execution-events.DLT";

    // ==================== PRODUCER CONFIG ====================
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Configurações de confiabilidade
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // Espera confirmação de todas as réplicas
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Evita duplicatas

        // Configurações de performance
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5); // Aguarda 5ms para agrupar mensagens
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ==================== CONSUMER CONFIG ====================
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Configurações de consumo
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Começa do início se não houver offset
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Commit manual após processamento
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);

        // Trust packages for deserialization
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "br.com.grupo99.*");
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Object.class.getName());

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // Configuração de commit manual
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // Error handler com DLT e backoff exponencial
        factory.setCommonErrorHandler(kafkaErrorHandler());

        // Concurrency (número de threads de consumo)
        factory.setConcurrency(3);

        return factory;
    }

    /**
     * Error Handler com Dead Letter Topic e Exponential Backoff
     * - 5 retries com backoff exponencial (1s, 2s, 4s, 8s, 16s)
     * - Após falhar todos os retries, envia para DLT
     */
    @Bean
    public CommonErrorHandler kafkaErrorHandler() {
        // Configura Dead Letter Publishing
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate(),
                (record, ex) -> {
                    // Determina o tópico DLT baseado no tópico original
                    String dltTopic = record.topic() + ".DLT";
                    log.error("🔴 Enviando mensagem para DLT: {}. Erro: {}", dltTopic, ex.getMessage());
                    return new org.apache.kafka.common.TopicPartition(dltTopic, record.partition());
                });

        // Exponential Backoff: 1s inicial, máximo 30s, multiplicador 2x
        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxElapsedTime(30000L); // Máximo 30 segundos total
        backOff.setMaxInterval(16000L); // Máximo 16 segundos entre retries

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // Não fazer retry para erros de deserialização
        errorHandler.addNotRetryableExceptions(
                org.apache.kafka.common.errors.SerializationException.class,
                org.springframework.messaging.converter.MessageConversionException.class);

        return errorHandler;
    }

    // ==================== TOPIC CREATION ====================
    @Bean
    public NewTopic osEventsTopic() {
        return TopicBuilder.name(TOPIC_OS_EVENTS)
                .partitions(6) // Partições para paralelismo
                .replicas(1) // 1 para dev, aumentar em prod
                .config("retention.ms", "2592000000") // 30 dias de retenção
                .config("cleanup.policy", "delete")
                .build();
    }

    @Bean
    public NewTopic billingEventsTopic() {
        return TopicBuilder.name(TOPIC_BILLING_EVENTS)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "2592000000")
                .build();
    }

    @Bean
    public NewTopic executionEventsTopic() {
        return TopicBuilder.name(TOPIC_EXECUTION_EVENTS)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "2592000000")
                .build();
    }

    // ==================== DEAD LETTER TOPICS ====================
    @Bean
    public NewTopic osEventsDltTopic() {
        return TopicBuilder.name(DLT_OS_EVENTS)
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "604800000") // 7 dias
                .build();
    }

    @Bean
    public NewTopic billingEventsDltTopic() {
        return TopicBuilder.name(DLT_BILLING_EVENTS)
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "604800000")
                .build();
    }

    @Bean
    public NewTopic executionEventsDltTopic() {
        return TopicBuilder.name(DLT_EXECUTION_EVENTS)
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "604800000")
                .build();
    }
}
