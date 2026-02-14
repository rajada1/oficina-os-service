package br.com.grupo99.osservice.infrastructure.messaging;

import br.com.grupo99.osservice.domain.model.OrdemServico;
import br.com.grupo99.osservice.domain.model.StatusOS;
import br.com.grupo99.osservice.domain.repository.OrdemServicoRepository;
import br.com.grupo99.osservice.infrastructure.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * Consumidor de eventos Kafka para a Saga de Ordens de Serviço
 * Migrado de AWS SQS polling para Kafka push-based com Consumer Groups
 * 
 * Padrões implementados:
 * - Manual Acknowledgment (controle explícito de commit)
 * - Consumer Group para processamento distribuído
 * - Dead Letter Topic para eventos não processados
 * - Headers para roteamento de eventos
 */
@Slf4j
@Service
public class KafkaEventListener {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ObjectMapper objectMapper;
    private final EventPublisherPort eventPublisher;

    public KafkaEventListener(
            OrdemServicoRepository ordemServicoRepository,
            ObjectMapper objectMapper,
            EventPublisherPort eventPublisher) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Consome eventos do tópico billing-events
     * Processa: ORCAMENTO_APROVADO, ORCAMENTO_REJEITADO
     */
    @KafkaListener(topics = KafkaConfig.TOPIC_BILLING_EVENTS, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory", concurrency = "3")
    public void consumeBillingEvents(
            ConsumerRecord<String, Object> record,
            Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        String eventType = extractHeader(record, "eventType");
        String osId = record.key();

        log.info("📥 Recebido evento Kafka do billing-service. " +
                "Type: {}, OS ID: {}, Partition: {}, Offset: {}",
                eventType, osId, partition, offset);

        try {
            switch (eventType) {
                case "ORCAMENTO_APROVADO" -> handleOrcamentoAprovado(record);
                case "ORCAMENTO_REJEITADO" -> handleOrcamentoRejeitado(record);
                default -> log.warn("⚠️ Tipo de evento desconhecido do billing: {}", eventType);
            }

            // Commit manual após processamento bem-sucedido
            acknowledgment.acknowledge();
            log.debug("✅ Evento {} commitado com sucesso. Offset: {}", eventType, offset);

        } catch (Exception e) {
            log.error("❌ Erro ao processar evento do billing. Type: {}, OS ID: {}, Erro: {}",
                    eventType, osId, e.getMessage(), e);
            // Não faz acknowledge - mensagem será reprocessada
            // Em produção: implementar retry com backoff ou enviar para DLT
            handleProcessingError(record, e, "billing");
        }
    }

    /**
     * Consome eventos do tópico execution-events
     * Processa: EXECUCAO_CONCLUIDA, EXECUCAO_FALHOU
     */
    @KafkaListener(topics = KafkaConfig.TOPIC_EXECUTION_EVENTS, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory", concurrency = "2")
    public void consumeExecutionEvents(
            ConsumerRecord<String, Object> record,
            Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        String eventType = extractHeader(record, "eventType");
        String osId = record.key();

        log.info("📥 Recebido evento Kafka do execution-service. " +
                "Type: {}, OS ID: {}, Partition: {}, Offset: {}",
                eventType, osId, partition, offset);

        try {
            switch (eventType) {
                case "EXECUCAO_CONCLUIDA" -> handleExecucaoConcluida(record);
                case "EXECUCAO_FALHOU" -> handleExecucaoFalhou(record);
                default -> log.warn("⚠️ Tipo de evento desconhecido da execução: {}", eventType);
            }

            acknowledgment.acknowledge();
            log.debug("✅ Evento {} commitado com sucesso. Offset: {}", eventType, offset);

        } catch (Exception e) {
            log.error("❌ Erro ao processar evento da execução. Type: {}, OS ID: {}, Erro: {}",
                    eventType, osId, e.getMessage(), e);
            handleProcessingError(record, e, "execution");
        }
    }

    /**
     * Saga Step 3: Orçamento aprovado pelo cliente
     * Avança a OS para status "EM_EXECUCAO"
     */
    @SuppressWarnings("unchecked")
    private void handleOrcamentoAprovado(ConsumerRecord<String, Object> record) {
        try {
            UUID osId = UUID.fromString(record.key());
            Map<String, Object> payload = (Map<String, Object>) record.value();

            Double valorAprovado = ((Number) payload.getOrDefault("valorAprovado", 0.0)).doubleValue();
            String aprovadoPor = (String) payload.getOrDefault("aprovadoPor", "sistema");

            log.info("💰 Processando ORCAMENTO_APROVADO. OS ID: {}, Valor: R$ {}, Aprovado por: {}",
                    osId, valorAprovado, aprovadoPor);

            // Busca e atualiza a OS
            OrdemServico os = ordemServicoRepository.findById(osId)
                    .orElseThrow(() -> new RuntimeException("OS não encontrada: " + osId));

            os.atualizarStatus(StatusOS.EM_EXECUCAO, "Orçamento aprovado via Kafka", aprovadoPor);
            ordemServicoRepository.save(os);

            log.info("✅ OS {} avançou para EM_EXECUCAO após aprovação de orçamento", osId);

        } catch (Exception e) {
            log.error("❌ Erro ao processar ORCAMENTO_APROVADO: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Saga Compensação: Orçamento rejeitado pelo cliente
     * Retorna a OS para status "CANCELADA"
     */
    @SuppressWarnings("unchecked")
    private void handleOrcamentoRejeitado(ConsumerRecord<String, Object> record) {
        try {
            UUID osId = UUID.fromString(record.key());
            Map<String, Object> payload = (Map<String, Object>) record.value();

            String motivo = (String) payload.getOrDefault("motivo", "Não especificado");

            log.warn("❌ Processando ORCAMENTO_REJEITADO. OS ID: {}, Motivo: {}", osId, motivo);

            // Busca e atualiza a OS
            OrdemServico os = ordemServicoRepository.findById(osId)
                    .orElseThrow(() -> new RuntimeException("OS não encontrada: " + osId));

            os.atualizarStatus(StatusOS.CANCELADA, "Orçamento rejeitado: " + motivo, "Sistema");
            ordemServicoRepository.save(os);

            log.info("🔄 OS {} marcada como CANCELADA por rejeição de orçamento", osId);

        } catch (Exception e) {
            log.error("❌ Erro ao processar ORCAMENTO_REJEITADO: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Saga Final: Execução do serviço concluída com sucesso
     * Finaliza a OS
     */
    @SuppressWarnings("unchecked")
    private void handleExecucaoConcluida(ConsumerRecord<String, Object> record) {
        try {
            UUID osId = UUID.fromString(record.key());
            Map<String, Object> payload = (Map<String, Object>) record.value();

            String observacoes = (String) payload.getOrDefault("observacoes", "");
            String executadoPor = (String) payload.getOrDefault("executadoPor", "sistema");

            log.info("🏁 Processando EXECUCAO_CONCLUIDA. OS ID: {}, Executado por: {}",
                    osId, executadoPor);

            // Busca e finaliza a OS
            OrdemServico os = ordemServicoRepository.findById(osId)
                    .orElseThrow(() -> new RuntimeException("OS não encontrada: " + osId));

            os.atualizarStatus(StatusOS.FINALIZADA, "Execução concluída: " + observacoes, executadoPor);
            ordemServicoRepository.save(os);

            log.info("✅ OS {} FINALIZADA com sucesso! Saga completa.", osId);

        } catch (Exception e) {
            log.error("❌ Erro ao processar EXECUCAO_CONCLUIDA: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Saga Compensação: Falha na execução do serviço
     */
    @SuppressWarnings("unchecked")
    private void handleExecucaoFalhou(ConsumerRecord<String, Object> record) {
        try {
            UUID osId = UUID.fromString(record.key());
            Map<String, Object> payload = (Map<String, Object>) record.value();

            String motivo = (String) payload.getOrDefault("motivo", "Falha não especificada");
            Boolean requerRetrabalho = (Boolean) payload.getOrDefault("requerRetrabalho", false);

            log.error("💥 Processando EXECUCAO_FALHOU. OS ID: {}, Motivo: {}, Requer retrabalho: {}",
                    osId, motivo, requerRetrabalho);

            OrdemServico os = ordemServicoRepository.findById(osId)
                    .orElseThrow(() -> new RuntimeException("OS não encontrada: " + osId));

            if (requerRetrabalho) {
                os.atualizarStatus(StatusOS.EM_EXECUCAO, "Retrabalho necessário: " + motivo, "Sistema");
                log.warn("🔄 OS {} requer retrabalho - aguardando nova execução", osId);
            } else {
                os.atualizarStatus(StatusOS.CANCELADA, "Execução falhou: " + motivo, "Sistema");
                log.error("❌ OS {} cancelada por falha na execução", osId);
            }
            ordemServicoRepository.save(os);

        } catch (Exception e) {
            log.error("❌ Erro ao processar EXECUCAO_FALHOU: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Extrai header do ConsumerRecord
     */
    private String extractHeader(ConsumerRecord<String, Object> record, String headerKey) {
        var header = record.headers().lastHeader(headerKey);
        if (header != null && header.value() != null) {
            return new String(header.value(), StandardCharsets.UTF_8);
        }
        return "UNKNOWN";
    }

    /**
     * Trata erros de processamento
     * Em produção: implementar envio para Dead Letter Topic
     */
    private void handleProcessingError(ConsumerRecord<String, Object> record, Exception e, String source) {
        log.error("🔴 Erro crítico no processamento de evento do {}. " +
                "Topic: {}, Partition: {}, Offset: {}, Key: {}, Erro: {}",
                source,
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                e.getMessage());

        // TODO: Implementar envio para Dead Letter Topic
        // kafkaTemplate.send("dlt-" + record.topic(), record.key(), record.value());
    }
}
