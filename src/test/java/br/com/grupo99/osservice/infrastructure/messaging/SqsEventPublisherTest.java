package br.com.grupo99.osservice.infrastructure.messaging;

import br.com.grupo99.osservice.application.events.OSCanceladaEvent;
import br.com.grupo99.osservice.application.events.OSCriadaEvent;
import br.com.grupo99.osservice.application.events.StatusMudadoEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventPublisher (SQS)")
class SqsEventPublisherTest {

    @Mock
    private SqsClient sqsClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    private EventPublisher publisher;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
        publisher = new EventPublisher(sqsClient, objectMapper);
        ReflectionTestUtils.setField(publisher, "osEventsQueueUrl", "http://sqs.test/queue");
    }

    @Test
    @DisplayName("publishOSCriada deve enviar mensagem SQS com sucesso")
    void publishOSCriada_deveEnviarComSucesso() {
        OSCriadaEvent event = new OSCriadaEvent(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), "Teste", LocalDateTime.now(), "OS_CRIADA");

        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().messageId("msg-1").build());

        publisher.publishOSCriada(event);

        verify(sqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    @DisplayName("publishOSCriada deve lançar RuntimeException quando falha")
    void publishOSCriada_deveLancarExcecaoQuandoFalha() {
        OSCriadaEvent event = new OSCriadaEvent(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), "Teste", LocalDateTime.now(), "OS_CRIADA");

        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(new RuntimeException("SQS error"));

        assertThatThrownBy(() -> publisher.publishOSCriada(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao publicar evento OS_CRIADA");
    }

    @Test
    @DisplayName("publishStatusMudado deve enviar mensagem SQS com sucesso")
    void publishStatusMudado_deveEnviarComSucesso() {
        StatusMudadoEvent event = new StatusMudadoEvent(UUID.randomUUID(),
                "ABERTA", "EM_DIAGNOSTICO", LocalDateTime.now(), "STATUS_MUDADO");

        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().messageId("msg-2").build());

        publisher.publishStatusMudado(event);

        verify(sqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    @DisplayName("publishStatusMudado não deve lançar exceção quando falha (não bloqueia transação)")
    void publishStatusMudado_naoDeveLancarExcecaoQuandoFalha() {
        StatusMudadoEvent event = new StatusMudadoEvent(UUID.randomUUID(),
                "ABERTA", "EM_DIAGNOSTICO", LocalDateTime.now(), "STATUS_MUDADO");

        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(new RuntimeException("SQS error"));

        // Should NOT throw
        publisher.publishStatusMudado(event);
    }

    @Test
    @DisplayName("publishOSCancelada deve enviar mensagem SQS com sucesso")
    void publishOSCancelada_deveEnviarComSucesso() {
        OSCanceladaEvent event = new OSCanceladaEvent(UUID.randomUUID(),
                "Motivo", "BILLING", LocalDateTime.now(), "OS_CANCELADA");

        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().messageId("msg-3").build());

        publisher.publishOSCancelada(event);

        verify(sqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    @DisplayName("publishOSCancelada não deve lançar exceção quando falha (compensação crítica)")
    void publishOSCancelada_naoDeveLancarExcecaoQuandoFalha() {
        OSCanceladaEvent event = new OSCanceladaEvent(UUID.randomUUID(),
                "Motivo", "BILLING", LocalDateTime.now(), "OS_CANCELADA");

        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(new RuntimeException("SQS error"));

        // Should NOT throw - just logs critical error
        publisher.publishOSCancelada(event);
    }
}
