package br.com.grupo99.osservice.infrastructure.messaging;

import br.com.grupo99.osservice.application.events.OrcamentoAprovadoEvent;
import br.com.grupo99.osservice.domain.model.OrdemServico;
import br.com.grupo99.osservice.domain.model.StatusOS;
import br.com.grupo99.osservice.domain.repository.OrdemServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventListener (SQS compensation)")
class SqsEventListenerTest {

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private EventPublisher eventPublisher;

    private EventListener eventListener;

    @BeforeEach
    void setUp() {
        eventListener = new EventListener(ordemServicoRepository, eventPublisher);
    }

    @Test
    @DisplayName("handleOrcamentoRejeitado deve reverter status para AGUARDANDO_APROVACAO quando EM_EXECUCAO")
    void handleOrcamentoRejeitado_deveReverterStatus() {
        UUID osId = UUID.randomUUID();
        OrcamentoAprovadoEvent event = new OrcamentoAprovadoEvent(UUID.randomUUID(), osId,
                LocalDateTime.now(), "ORCAMENTO_APROVADO");

        OrdemServico mockOS = mock(OrdemServico.class);
        when(mockOS.getStatus()).thenReturn(StatusOS.EM_EXECUCAO);
        when(ordemServicoRepository.findById(osId)).thenReturn(Optional.of(mockOS));

        eventListener.handleOrcamentoRejeitado(event);

        verify(mockOS).setStatus(StatusOS.AGUARDANDO_APROVACAO);
        verify(ordemServicoRepository).save(mockOS);
    }

    @Test
    @DisplayName("handleOrcamentoRejeitado não deve reverter quando status não é EM_EXECUCAO")
    void handleOrcamentoRejeitado_naoDeveReverterQuandoStatusDiferente() {
        UUID osId = UUID.randomUUID();
        OrcamentoAprovadoEvent event = new OrcamentoAprovadoEvent(UUID.randomUUID(), osId,
                LocalDateTime.now(), "ORCAMENTO_APROVADO");

        OrdemServico mockOS = mock(OrdemServico.class);
        when(mockOS.getStatus()).thenReturn(StatusOS.RECEBIDA);
        when(ordemServicoRepository.findById(osId)).thenReturn(Optional.of(mockOS));

        eventListener.handleOrcamentoRejeitado(event);

        verify(mockOS, never()).setStatus(any(StatusOS.class));
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    @DisplayName("handleOrcamentoRejeitado deve lidar com OS não encontrada sem lançar exceção")
    void handleOrcamentoRejeitado_deveLidarComOSNaoEncontrada() {
        UUID osId = UUID.randomUUID();
        OrcamentoAprovadoEvent event = new OrcamentoAprovadoEvent(UUID.randomUUID(), osId,
                LocalDateTime.now(), "ORCAMENTO_APROVADO");

        when(ordemServicoRepository.findById(osId)).thenReturn(Optional.empty());

        // Should not throw - just logs critical error
        eventListener.handleOrcamentoRejeitado(event);
    }

    @Test
    @DisplayName("handleExecucaoFalhou deve cancelar OS")
    void handleExecucaoFalhou_deveCancelarOS() {
        UUID osId = UUID.randomUUID();
        OrdemServico mockOS = mock(OrdemServico.class);
        when(ordemServicoRepository.findById(osId)).thenReturn(Optional.of(mockOS));

        eventListener.handleExecucaoFalhou(osId.toString(), "Peça defeituosa");

        verify(mockOS).setStatus(StatusOS.CANCELADA);
        verify(ordemServicoRepository).save(mockOS);
    }

    @Test
    @DisplayName("handleExecucaoFalhou deve lidar com OS não encontrada sem lançar exceção")
    void handleExecucaoFalhou_deveLidarComOSNaoEncontrada() {
        UUID osId = UUID.randomUUID();
        when(ordemServicoRepository.findById(osId)).thenReturn(Optional.empty());

        // Should not throw - just logs critical error
        eventListener.handleExecucaoFalhou(osId.toString(), "Erro");
    }
}
