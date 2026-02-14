package br.com.grupo99.osservice.infrastructure.messaging;

import br.com.grupo99.osservice.application.events.OSCanceladaEvent;
import br.com.grupo99.osservice.application.events.OSCriadaEvent;
import br.com.grupo99.osservice.application.events.StatusMudadoEvent;
import br.com.grupo99.osservice.infrastructure.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Publicador de eventos para Apache Kafka (Saga Pattern - Event Publisher)
 * Migrado de AWS SQS para arquitetura de eventos com Kafka.
 * 
 * Benefícios:
 * - Ordenação garantida por partição (usando osId como key)
 * - Múltiplos consumidores via Consumer Groups
 * - Replay de eventos para Event Sourcing
 * - Alta throughput e baixa latência
 * 
 * Resiliência:
 * - Circuit Breaker para proteção contra falhas do broker
 * - Retry com backoff exponencial
 * - Time limiter para evitar bloqueios
 */
@Slf4j
@Service
@Primary
public class KafkaEventPublisher implements EventPublisherPort {

    private static final String CIRCUIT_BREAKER_NAME = "kafkaPublisher";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publica evento de OS criada (Saga Step 1)
     * Partição baseada no osId para garantir ordenação de eventos da mesma OS
     */
    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "publishOSCriadaFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public void publishOSCriada(OSCriadaEvent event) {
        String key = event.getOsId().toString();

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaConfig.TOPIC_OS_EVENTS,
                null, // partition (null = use key hash)
                key,
                event);

        // Headers para facilitar roteamento e filtragem
        record.headers()
                .add(new RecordHeader("eventType", "OS_CRIADA".getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("osId", key.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("timestamp", event.getTimestamp().toString().getBytes(StandardCharsets.UTF_8)));

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(record);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ Evento OS_CRIADA publicado no Kafka. " +
                        "OS ID: {}, Topic: {}, Partition: {}, Offset: {}",
                        event.getOsId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("❌ Erro ao publicar evento OS_CRIADA no Kafka: {}", ex.getMessage(), ex);
            }
        });
    }

    /**
     * Fallback quando Circuit Breaker está aberto para OS_CRIADA
     */
    public void publishOSCriadaFallback(OSCriadaEvent event, Throwable t) {
        log.error("🔴 Circuit Breaker ABERTO - Evento OS_CRIADA não publicado. OS ID: {}, Erro: {}",
                event.getOsId(), t.getMessage());
        // Aqui poderia salvar em outbox table para retry posterior
        // ou enviar para Dead Letter Topic manualmente
    }

    /**
     * Publica evento de mudança de status (Saga Step intermediário)
     */
    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "publishStatusMudadoFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public void publishStatusMudado(StatusMudadoEvent event) {
        String key = event.getOsId().toString();

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaConfig.TOPIC_OS_EVENTS,
                key,
                event);

        record.headers()
                .add(new RecordHeader("eventType", "STATUS_MUDADO".getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("osId", key.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("statusAnterior", event.getStatusAnterior().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("statusNovo", event.getStatusNovo().getBytes(StandardCharsets.UTF_8)));

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(record);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ Evento STATUS_MUDADO publicado. OS ID: {}, {} -> {}, Partition: {}, Offset: {}",
                        event.getOsId(),
                        event.getStatusAnterior(),
                        event.getStatusNovo(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("❌ Erro ao publicar evento STATUS_MUDADO: {}", ex.getMessage(), ex);
            }
        });
    }

    /**
     * Fallback quando Circuit Breaker está aberto para STATUS_MUDADO
     */
    public void publishStatusMudadoFallback(StatusMudadoEvent event, Throwable t) {
        log.error("🔴 Circuit Breaker ABERTO - Evento STATUS_MUDADO não publicado. OS ID: {}, Erro: {}",
                event.getOsId(), t.getMessage());
    }

    /**
     * Publica evento de compensação - OS cancelada (Rollback)
     */
    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "publishOSCanceladaFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public void publishOSCancelada(OSCanceladaEvent event) {
        String key = event.getOsId().toString();

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaConfig.TOPIC_OS_EVENTS,
                key,
                event);

        record.headers()
                .add(new RecordHeader("eventType", "OS_CANCELADA".getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("osId", key.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("motivo", event.getMotivo().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("etapaFalha", event.getEtapaFalha().getBytes(StandardCharsets.UTF_8)));

        // Envio síncrono para eventos de compensação (mais críticos)
        try {
            SendResult<String, Object> result = kafkaTemplate.send(record).get();
            log.warn("🔄 Evento de compensação OS_CANCELADA publicado. " +
                    "OS ID: {}, Motivo: {}, Partition: {}, Offset: {}",
                    event.getOsId(),
                    event.getMotivo(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        } catch (Exception e) {
            log.error("❌ ERRO CRÍTICO ao publicar evento de compensação OS_CANCELADA: {}", e.getMessage(), e);
            // Alerta crítico - necessita intervenção manual
            throw new RuntimeException("Falha ao publicar evento de compensação", e);
        }
    }

    /**
     * Fallback quando Circuit Breaker está aberto para OS_CANCELADA
     */
    public void publishOSCanceladaFallback(OSCanceladaEvent event, Throwable t) {
        log.error(
                "🔴 Circuit Breaker ABERTO - Evento CRÍTICO OS_CANCELADA não publicado. OS ID: {}, Motivo: {}, Erro: {}",
                event.getOsId(), event.getMotivo(), t.getMessage());
        // CRÍTICO: Evento de compensação deve ser persistido para retry manual
        // Aqui poderia salvar em outbox table ou alertar operações
    }

    /**
     * Publica evento genérico para um tópico específico
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "publishEventFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public void publishEvent(String topic, String key, Object event, String eventType) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, event);
        record.headers().add(new RecordHeader("eventType", eventType.getBytes(StandardCharsets.UTF_8)));

        kafkaTemplate.send(record).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ Evento {} publicado no tópico {}. Key: {}, Partition: {}, Offset: {}",
                        eventType, topic, key,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("❌ Erro ao publicar evento {} no tópico {}: {}", eventType, topic, ex.getMessage(), ex);
            }
        });
    }

    /**
     * Fallback genérico para eventos
     */
    public void publishEventFallback(String topic, String key, Object event, String eventType, Throwable t) {
        log.error("🔴 Circuit Breaker ABERTO - Evento {} não publicado no tópico {}. Key: {}, Erro: {}",
                eventType, topic, key, t.getMessage());
    }
}
