package br.com.grupo99.osservice.infrastructure.messaging;

import br.com.grupo99.osservice.application.events.OSCanceladaEvent;
import br.com.grupo99.osservice.application.events.OSCriadaEvent;
import br.com.grupo99.osservice.application.events.StatusMudadoEvent;
import br.com.grupo99.osservice.infrastructure.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para o KafkaEventPublisher usando Embedded Kafka
 * Utiliza o bean gerenciado pelo Spring para suportar Resilience4j
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = {
        KafkaConfig.TOPIC_OS_EVENTS,
        KafkaConfig.TOPIC_BILLING_EVENTS,
        KafkaConfig.TOPIC_EXECUTION_EVENTS
}, brokerProperties = {
        "listeners=PLAINTEXT://localhost:9093",
        "port=9093"
})
class KafkaEventPublisherTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventPublisherPort eventPublisher; // Usar o bean do Spring com Resilience4j

    private Consumer<String, Object> consumer;

    @BeforeEach
    void setUp() {
        // Configurar consumer para testes
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "test-consumer-group-" + UUID.randomUUID(), "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        ConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = consumerFactory.createConsumer();
        consumer.subscribe(Collections.singletonList(KafkaConfig.TOPIC_OS_EVENTS));
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    @DisplayName("Deve publicar evento OS_CRIADA no tópico os-events")
    void devePublicarEventoOSCriada() throws InterruptedException {
        // Arrange
        UUID osId = UUID.randomUUID();
        OSCriadaEvent event = new OSCriadaEvent(
                osId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Troca de óleo e filtro",
                LocalDateTime.now(),
                "OS_CRIADA");

        // Act
        eventPublisher.publishOSCriada(event);

        // Aguardar o envio assíncrono
        Thread.sleep(2000);

        // Assert - buscar até encontrar o evento com a key correta
        var foundRecord = pollForRecordWithKey(consumer, osId.toString(), Duration.ofSeconds(10));
        assertThat(foundRecord).isNotNull();
        assertThat(foundRecord.topic()).isEqualTo(KafkaConfig.TOPIC_OS_EVENTS);

        // Verificar headers
        var eventTypeHeader = foundRecord.headers().lastHeader("eventType");
        assertThat(eventTypeHeader).isNotNull();
        assertThat(new String(eventTypeHeader.value())).isEqualTo("OS_CRIADA");
    }

    @Test
    @DisplayName("Deve publicar evento STATUS_MUDADO no tópico os-events")
    void devePublicarEventoStatusMudado() throws InterruptedException {
        // Arrange
        UUID osId = UUID.randomUUID();
        StatusMudadoEvent event = new StatusMudadoEvent(
                osId,
                "ABERTA",
                "EM_DIAGNOSTICO",
                LocalDateTime.now(),
                "STATUS_MUDADO");

        // Act
        eventPublisher.publishStatusMudado(event);

        // Aguardar o envio assíncrono
        Thread.sleep(2000);

        // Assert - buscar até encontrar o evento com a key correta
        var foundRecord = pollForRecordWithKey(consumer, osId.toString(), Duration.ofSeconds(10));
        assertThat(foundRecord).isNotNull();

        // Verificar headers de status
        var statusAnteriorHeader = foundRecord.headers().lastHeader("statusAnterior");
        var statusNovoHeader = foundRecord.headers().lastHeader("statusNovo");

        assertThat(new String(statusAnteriorHeader.value())).isEqualTo("ABERTA");
        assertThat(new String(statusNovoHeader.value())).isEqualTo("EM_DIAGNOSTICO");
    }

    @Test
    @DisplayName("Deve publicar evento OS_CANCELADA de forma síncrona")
    void devePublicarEventoOSCancelada() throws InterruptedException {
        // Arrange
        UUID osId = UUID.randomUUID();
        OSCanceladaEvent event = new OSCanceladaEvent(
                osId,
                "Cliente cancelou",
                "BILLING",
                LocalDateTime.now(),
                "OS_CANCELADA");

        // Act
        eventPublisher.publishOSCancelada(event);

        // Aguardar um pouco para garantir processamento
        Thread.sleep(1000);

        // Assert - buscar até encontrar o evento com a key correta
        var foundRecord = pollForRecordWithKey(consumer, osId.toString(), Duration.ofSeconds(10));
        assertThat(foundRecord).isNotNull();

        // Verificar headers de compensação
        var motivoHeader = foundRecord.headers().lastHeader("motivo");
        var etapaFalhaHeader = foundRecord.headers().lastHeader("etapaFalha");

        assertThat(new String(motivoHeader.value())).isEqualTo("Cliente cancelou");
        assertThat(new String(etapaFalhaHeader.value())).isEqualTo("BILLING");
    }

    @Test
    @DisplayName("Deve usar osId como partition key para ordenação")
    void deveUsarOsIdComoPartitionKey() throws InterruptedException {
        // Arrange - Múltiplos eventos para mesma OS
        UUID osId = UUID.randomUUID();

        OSCriadaEvent criada = new OSCriadaEvent(osId, UUID.randomUUID(), UUID.randomUUID(),
                "Revisão completa", LocalDateTime.now(), "OS_CRIADA");
        StatusMudadoEvent status = new StatusMudadoEvent(osId, "ABERTA", "EM_DIAGNOSTICO",
                LocalDateTime.now(), "STATUS_MUDADO");

        // Act
        eventPublisher.publishOSCriada(criada);
        eventPublisher.publishStatusMudado(status);

        Thread.sleep(2000);

        // Assert - Verificar que pelo menos encontra eventos com a key correta
        var records = pollAllRecordsWithKey(consumer, osId.toString(), Duration.ofSeconds(10));
        assertThat(records).isNotEmpty();

        records.forEach(record -> {
            assertThat(record.key()).isEqualTo(osId.toString());
        });
    }

    // ===================== HELPER METHODS =====================

    /**
     * Poll para encontrar um registro específico por key
     */
    private org.apache.kafka.clients.consumer.ConsumerRecord<String, Object> pollForRecordWithKey(
            Consumer<String, Object> consumer, String key, Duration timeout) {

        long endTime = System.currentTimeMillis() + timeout.toMillis();

        while (System.currentTimeMillis() < endTime) {
            ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(500));
            for (var record : records) {
                if (record.key().equals(key)) {
                    return record;
                }
            }
        }
        return null;
    }

    /**
     * Poll para encontrar todos os registros com uma key específica
     */
    private java.util.List<org.apache.kafka.clients.consumer.ConsumerRecord<String, Object>> pollAllRecordsWithKey(
            Consumer<String, Object> consumer, String key, Duration timeout) {

        java.util.List<org.apache.kafka.clients.consumer.ConsumerRecord<String, Object>> result = new java.util.ArrayList<>();
        long endTime = System.currentTimeMillis() + timeout.toMillis();

        while (System.currentTimeMillis() < endTime) {
            ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(500));
            for (var record : records) {
                if (record.key().equals(key)) {
                    result.add(record);
                }
            }
            // Se já encontrou registros, pode parar após mais um poll vazio
            if (!result.isEmpty() && records.isEmpty()) {
                break;
            }
        }
        return result;
    }
}