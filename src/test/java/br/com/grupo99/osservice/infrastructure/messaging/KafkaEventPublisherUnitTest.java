package br.com.grupo99.osservice.infrastructure.messaging;

import br.com.grupo99.osservice.application.events.OSCanceladaEvent;
import br.com.grupo99.osservice.application.events.OSCriadaEvent;
import br.com.grupo99.osservice.application.events.StatusMudadoEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaEventPublisher - Unit Tests")
class KafkaEventPublisherUnitTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private KafkaEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new KafkaEventPublisher(kafkaTemplate, objectMapper);
    }

    @SuppressWarnings("unchecked")
    private SendResult<String, Object> createMockSendResult(String topic, int partition, long offset) {
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(topic, partition), offset, 0, 0L, 0, 0);
        SendResult<String, Object> sendResult = mock(SendResult.class);
        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        return sendResult;
    }

    @Test
    @DisplayName("publishOSCriada deve enviar ProducerRecord com headers corretos")
    void publishOSCriada_deveEnviarComHeadersCorretos() {
        UUID osId = UUID.randomUUID();
        OSCriadaEvent event = new OSCriadaEvent(osId, UUID.randomUUID(), UUID.randomUUID(),
                "Teste", LocalDateTime.now(), "OS_CRIADA");

        SendResult<String, Object> sendResult = createMockSendResult("os-events", 0, 1L);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        publisher.publishOSCriada(event);

        ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());

        ProducerRecord<String, Object> record = captor.getValue();
        assertThat(record.key()).isEqualTo(osId.toString());
        assertThat(record.topic()).isEqualTo("os-events");
        assertThat(record.headers().lastHeader("eventType")).isNotNull();
        assertThat(new String(record.headers().lastHeader("eventType").value())).isEqualTo("OS_CRIADA");
        assertThat(record.headers().lastHeader("osId")).isNotNull();
        assertThat(record.headers().lastHeader("timestamp")).isNotNull();
    }

    @Test
    @DisplayName("publishStatusMudado deve enviar ProducerRecord com headers de status")
    void publishStatusMudado_deveEnviarComHeadersDeStatus() {
        UUID osId = UUID.randomUUID();
        StatusMudadoEvent event = new StatusMudadoEvent(osId, "ABERTA", "EM_DIAGNOSTICO",
                LocalDateTime.now(), "STATUS_MUDADO");

        SendResult<String, Object> sendResult = createMockSendResult("os-events", 0, 1L);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        publisher.publishStatusMudado(event);

        ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());

        ProducerRecord<String, Object> record = captor.getValue();
        assertThat(record.key()).isEqualTo(osId.toString());
        assertThat(new String(record.headers().lastHeader("eventType").value())).isEqualTo("STATUS_MUDADO");
        assertThat(new String(record.headers().lastHeader("statusAnterior").value())).isEqualTo("ABERTA");
        assertThat(new String(record.headers().lastHeader("statusNovo").value())).isEqualTo("EM_DIAGNOSTICO");
    }

    @Test
    @DisplayName("publishOSCancelada deve enviar de forma síncrona")
    void publishOSCancelada_deveEnviarSincronamente() throws Exception {
        UUID osId = UUID.randomUUID();
        OSCanceladaEvent event = new OSCanceladaEvent(osId, "Motivo teste", "BILLING",
                LocalDateTime.now(), "OS_CANCELADA");

        SendResult<String, Object> sendResult = createMockSendResult("os-events", 0, 1L);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        publisher.publishOSCancelada(event);

        ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());

        ProducerRecord<String, Object> record = captor.getValue();
        assertThat(record.key()).isEqualTo(osId.toString());
        assertThat(new String(record.headers().lastHeader("eventType").value())).isEqualTo("OS_CANCELADA");
        assertThat(new String(record.headers().lastHeader("motivo").value())).isEqualTo("Motivo teste");
        assertThat(new String(record.headers().lastHeader("etapaFalha").value())).isEqualTo("BILLING");
    }

    @Test
    @DisplayName("publishOSCancelada deve lançar RuntimeException quando envio falha")
    void publishOSCancelada_deveLancarExcecaoQuandoFalha() {
        UUID osId = UUID.randomUUID();
        OSCanceladaEvent event = new OSCanceladaEvent(osId, "Motivo", "BILLING",
                LocalDateTime.now(), "OS_CANCELADA");

        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka unavailable"));
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(failedFuture);

        assertThatThrownBy(() -> publisher.publishOSCancelada(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao publicar evento de compensação");
    }

    @Test
    @DisplayName("publishEvent genérico deve enviar com eventType header")
    void publishEvent_deveEnviarComEventTypeHeader() {
        SendResult<String, Object> sendResult = createMockSendResult("custom-topic", 0, 1L);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        publisher.publishEvent("custom-topic", "key-1", "payload", "CUSTOM_EVENT");

        ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());

        ProducerRecord<String, Object> record = captor.getValue();
        assertThat(record.topic()).isEqualTo("custom-topic");
        assertThat(record.key()).isEqualTo("key-1");
        assertThat(new String(record.headers().lastHeader("eventType").value())).isEqualTo("CUSTOM_EVENT");
    }

    @Test
    @DisplayName("Fallback de OSCriada não deve lançar exceção")
    void publishOSCriadaFallback_naoDeveLancarExcecao() {
        UUID osId = UUID.randomUUID();
        OSCriadaEvent event = new OSCriadaEvent(osId, UUID.randomUUID(), UUID.randomUUID(),
                "Teste", LocalDateTime.now(), "OS_CRIADA");

        // Should not throw - just logs
        publisher.publishOSCriadaFallback(event, new RuntimeException("CB Open"));
    }

    @Test
    @DisplayName("Fallback de StatusMudado não deve lançar exceção")
    void publishStatusMudadoFallback_naoDeveLancarExcecao() {
        UUID osId = UUID.randomUUID();
        StatusMudadoEvent event = new StatusMudadoEvent(osId, "A", "B",
                LocalDateTime.now(), "STATUS_MUDADO");

        publisher.publishStatusMudadoFallback(event, new RuntimeException("CB Open"));
    }

    @Test
    @DisplayName("Fallback de OSCancelada não deve lançar exceção")
    void publishOSCanceladaFallback_naoDeveLancarExcecao() {
        UUID osId = UUID.randomUUID();
        OSCanceladaEvent event = new OSCanceladaEvent(osId, "Motivo", "BILLING",
                LocalDateTime.now(), "OS_CANCELADA");

        publisher.publishOSCanceladaFallback(event, new RuntimeException("CB Open"));
    }

    @Test
    @DisplayName("Fallback genérico de publishEvent não deve lançar exceção")
    void publishEventFallback_naoDeveLancarExcecao() {
        publisher.publishEventFallback("topic", "key", "payload", "EVENT_TYPE",
                new RuntimeException("CB Open"));
    }

    @Test
    @DisplayName("publishOSCriada deve lidar com erro async no callback")
    void publishOSCriada_deveLidarComErroAsync() {
        UUID osId = UUID.randomUUID();
        OSCriadaEvent event = new OSCriadaEvent(osId, UUID.randomUUID(), UUID.randomUUID(),
                "Teste", LocalDateTime.now(), "OS_CRIADA");

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Send failed"));
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        // Should not throw - error is handled in the whenComplete callback
        publisher.publishOSCriada(event);

        verify(kafkaTemplate).send(any(ProducerRecord.class));
    }

    @Test
    @DisplayName("publishStatusMudado deve lidar com erro async no callback")
    void publishStatusMudado_deveLidarComErroAsync() {
        UUID osId = UUID.randomUUID();
        StatusMudadoEvent event = new StatusMudadoEvent(osId, "A", "B",
                LocalDateTime.now(), "STATUS_MUDADO");

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Send failed"));
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        publisher.publishStatusMudado(event);

        verify(kafkaTemplate).send(any(ProducerRecord.class));
    }

    @Test
    @DisplayName("publishEvent genérico deve lidar com erro async")
    void publishEvent_deveLidarComErroAsync() {
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Send failed"));
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        publisher.publishEvent("topic", "key", "payload", "EVENT_TYPE");

        verify(kafkaTemplate).send(any(ProducerRecord.class));
    }
}
