package br.com.grupo99.osservice.infrastructure.messaging;

import br.com.grupo99.osservice.application.events.OSCanceladaEvent;
import br.com.grupo99.osservice.application.events.OrcamentoAprovadoEvent;
import br.com.grupo99.osservice.domain.model.OrdemServico;
import br.com.grupo99.osservice.domain.model.StatusOS;
import br.com.grupo99.osservice.domain.repository.OrdemServicoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Event Listener - Compensação Saga Pattern
 * Métodos de compensação para eventos de falha
 */
@Slf4j
@Component
public class EventListener {

    private final OrdemServicoRepository ordemServicoRepository;
    private final EventPublisher eventPublisher;

    public EventListener(OrdemServicoRepository ordemServicoRepository,
            EventPublisher eventPublisher) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * COMPENSAÇÃO: Quando orçamento é rejeitado, voltar OS para status anterior
     */
    public void handleOrcamentoRejeitado(OrcamentoAprovadoEvent event) {
        try {
            log.warn("🔄 Iniciando compensação: Orçamento rejeitado para OS: {}", event.getOsId());

            OrdemServico os = ordemServicoRepository.findById(event.getOsId())
                    .orElseThrow(() -> new RuntimeException("OS não encontrada: " + event.getOsId()));

            if (os.getStatus() == StatusOS.EM_EXECUCAO) {
                os.setStatus(StatusOS.AGUARDANDO_APROVACAO);
                ordemServicoRepository.save(os);

                log.warn("✅ Compensação concluída: OS {} revertida para AGUARDANDO_APROVACAO", event.getOsId());
            } else {
                log.info("OS {} já está em status {}. Compensação não necessária.",
                        event.getOsId(), os.getStatus());
            }

        } catch (Exception e) {
            log.error("❌ ERRO CRÍTICO na compensação da OS {}: {}", event.getOsId(), e.getMessage(), e);
            // Alerta crítico - necessita intervenção manual
        }
    }

    /**
     * COMPENSAÇÃO: Se execução falhar, cancelar a OS completamente
     */
    public void handleExecucaoFalhou(String osId, String motivo) {
        try {
            log.error("🔄 Iniciando compensação: Execução falhou para OS: {}", osId);

            OrdemServico os = ordemServicoRepository.findById(java.util.UUID.fromString(osId))
                    .orElseThrow(() -> new RuntimeException("OS não encontrada: " + osId));

            // Cancelar completamente a OS
            os.setStatus(StatusOS.CANCELADA);
            ordemServicoRepository.save(os);

            log.error("✅ Compensação concluída: OS {} CANCELADA devido à falha na execução. Motivo: {}",
                    osId, motivo);

        } catch (Exception e) {
            log.error("❌ ERRO CRÍTICO na compensação da OS {}: {}", osId, e.getMessage(), e);
            // Alerta crítico - necessita intervenção manual
        }
    }
}
