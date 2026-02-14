package br.com.grupo99.osservice.infrastructure.messaging;

import br.com.grupo99.osservice.application.events.OSCanceladaEvent;
import br.com.grupo99.osservice.application.events.OSCriadaEvent;
import br.com.grupo99.osservice.application.events.StatusMudadoEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/**
 * Publicador de eventos para o SQS (Saga Pattern - Event Publisher)
 */
@Slf4j
@Service
public class EventPublisher {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.os-events-queue}")
    private String osEventsQueueUrl;

    public EventPublisher(SqsClient sqsClient, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Publica evento de OS criada (Saga Step 1)
     */
    public void publishOSCriada(OSCriadaEvent event) {
        try {
            String messageBody = objectMapper.writeValueAsString(event);

            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(osEventsQueueUrl)
                    .messageBody(messageBody)
                                        .messageDeduplicationId(event.getOsId().toString() + "-" + event.getTimestamp())
                    .build();

            sqsClient.sendMessage(sendMsgRequest);

            log.info("Evento OS_CRIADA publicado com sucesso. OS ID: {}", event.getOsId());
        } catch (Exception e) {
            log.error("Erro ao publicar evento OS_CRIADA: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao publicar evento OS_CRIADA", e);
        }
    }

    /**
     * Publica evento de mudança de status (Saga Step intermediário)
     */
    public void publishStatusMudado(StatusMudadoEvent event) {
        try {
            String messageBody = objectMapper.writeValueAsString(event);

            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(osEventsQueueUrl)
                    .messageBody(messageBody)
                                        .messageDeduplicationId(event.getOsId().toString() + "-" + event.getTimestamp())
                    .build();

            sqsClient.sendMessage(sendMsgRequest);

            log.info("Evento STATUS_MUDADO publicado. OS ID: {}, Status: {} -> {}",
                    event.getOsId(), event.getStatusAnterior(), event.getStatusNovo());
        } catch (Exception e) {
            log.error("Erro ao publicar evento STATUS_MUDADO: {}", e.getMessage(), e);
            // Não lança exceção para não bloquear a transação local
        }
    }

    /**
     * Publica evento de compensação - OS cancelada (Rollback)
     */
    public void publishOSCancelada(OSCanceladaEvent event) {
        try {
            String messageBody = objectMapper.writeValueAsString(event);

            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(osEventsQueueUrl)
                    .messageBody(messageBody)
                                        .messageDeduplicationId(event.getOsId().toString() + "-cancelled-" + event.getTimestamp())
                    .build();

            sqsClient.sendMessage(sendMsgRequest);

            log.warn("🔄 Evento de compensação OS_CANCELADA publicado. OS ID: {}, Motivo: {}",
                    event.getOsId(), event.getMotivo());
        } catch (Exception e) {
            log.error("Erro crítico ao publicar evento de compensação OS_CANCELADA: {}", e.getMessage(), e);
            // Alerta crítico - necessita intervenção manual
        }
    }
}
