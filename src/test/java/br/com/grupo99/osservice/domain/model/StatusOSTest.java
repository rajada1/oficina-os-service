package br.com.grupo99.osservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StatusOS - Testes Unitários")
class StatusOSTest {

    @Test
    @DisplayName("Deve permitir transição de ABERTA para EM_DIAGNOSTICO")
    void devePermitirTransicaoDeAbertaParaEmDiagnostico() {
        // ABERTA (RECEBIDA) deve transitar para EM_DIAGNOSTICO primeiro
        assertTrue(StatusOS.ABERTA.podeTransicionarPara(StatusOS.EM_DIAGNOSTICO));
        // Depois EM_DIAGNOSTICO pode ir para AGUARDANDO_ORCAMENTO
        assertTrue(StatusOS.EM_DIAGNOSTICO.podeTransicionarPara(StatusOS.AGUARDANDO_ORCAMENTO));
    }

    @Test
    @DisplayName("Deve permitir transição de AGUARDANDO_ORCAMENTO para ORCAMENTO_APROVADO")
    void devePermitirTransicaoDeAguardandoOrcamentoParaOrcamentoAprovado() {
        assertTrue(StatusOS.AGUARDANDO_ORCAMENTO.podeTransicionarPara(StatusOS.ORCAMENTO_APROVADO));
    }

    @Test
    @DisplayName("Deve permitir transição de ORCAMENTO_APROVADO para EM_EXECUCAO")
    void devePermitirTransicaoDeOrcamentoAprovadoParaEmExecucao() {
        assertTrue(StatusOS.ORCAMENTO_APROVADO.podeTransicionarPara(StatusOS.EM_EXECUCAO));
    }

    @Test
    @DisplayName("Deve permitir transição de EM_EXECUCAO para CONCLUIDA")
    void devePermitirTransicaoDeEmExecucaoParaConcluida() {
        assertTrue(StatusOS.EM_EXECUCAO.podeTransicionarPara(StatusOS.CONCLUIDA));
    }

    @Test
    @DisplayName("Deve permitir cancelamento de qualquer status exceto ENTREGUE e CANCELADA")
    void devePermitirCancelamentoDeQualquerStatusExcetoFinais() {
        // Pode cancelar estados intermediários via podeTransicionarPara
        assertTrue(StatusOS.ABERTA.podeTransicionarPara(StatusOS.CANCELADA));
        assertTrue(StatusOS.EM_DIAGNOSTICO.podeTransicionarPara(StatusOS.CANCELADA));
        assertTrue(StatusOS.AGUARDANDO_ORCAMENTO.podeTransicionarPara(StatusOS.CANCELADA));
        assertTrue(StatusOS.ORCAMENTO_APROVADO.podeTransicionarPara(StatusOS.CANCELADA));

        // EM_EXECUCAO não pode transitar diretamente para CANCELADA via
        // podeTransicionarPara
        // (a lógica de negócio exige finalizar ou requerer nova aprovação)
        // Mas pode ser cancelado via método cancelar() que não usa podeTransicionarPara
        assertFalse(StatusOS.EM_EXECUCAO.podeTransicionarPara(StatusOS.CANCELADA));
    }

    @Test
    @DisplayName("Não deve permitir transições inválidas")
    void naoDevePermitirTransicoesInvalidas() {
        // ABERTA (RECEBIDA) só pode ir para EM_DIAGNOSTICO ou CANCELADA
        assertFalse(StatusOS.ABERTA.podeTransicionarPara(StatusOS.AGUARDANDO_ORCAMENTO)); // Precisa passar por
                                                                                          // EM_DIAGNOSTICO
        assertFalse(StatusOS.ABERTA.podeTransicionarPara(StatusOS.ORCAMENTO_APROVADO));
        assertFalse(StatusOS.ABERTA.podeTransicionarPara(StatusOS.EM_EXECUCAO));
        assertFalse(StatusOS.ABERTA.podeTransicionarPara(StatusOS.CONCLUIDA));

        // AGUARDANDO_ORCAMENTO (AGUARDANDO_APROVACAO) pode ir para ORCAMENTO_APROVADO,
        // CANCELADA ou voltar para EM_DIAGNOSTICO
        assertFalse(StatusOS.AGUARDANDO_ORCAMENTO.podeTransicionarPara(StatusOS.ABERTA));
        assertFalse(StatusOS.AGUARDANDO_ORCAMENTO.podeTransicionarPara(StatusOS.EM_EXECUCAO));
        assertFalse(StatusOS.AGUARDANDO_ORCAMENTO.podeTransicionarPara(StatusOS.CONCLUIDA));

        // ORCAMENTO_APROVADO (AGUARDANDO_PAGAMENTO) só pode ir para EM_EXECUCAO ou
        // CANCELADA
        assertFalse(StatusOS.ORCAMENTO_APROVADO.podeTransicionarPara(StatusOS.ABERTA));
        assertFalse(StatusOS.ORCAMENTO_APROVADO.podeTransicionarPara(StatusOS.AGUARDANDO_ORCAMENTO));
        assertFalse(StatusOS.ORCAMENTO_APROVADO.podeTransicionarPara(StatusOS.CONCLUIDA));

        // EM_EXECUCAO pode ir para CONCLUIDA ou AGUARDANDO_ORCAMENTO (nova aprovação
        // necessária)
        assertFalse(StatusOS.EM_EXECUCAO.podeTransicionarPara(StatusOS.ABERTA));
        assertFalse(StatusOS.EM_EXECUCAO.podeTransicionarPara(StatusOS.ORCAMENTO_APROVADO));
    }

    @Test
    @DisplayName("CONCLUIDA (FINALIZADA) só pode transitar para ENTREGUE")
    void concluidaPodeApenasTransitarParaEntregue() {
        // FINALIZADA pode transitar apenas para ENTREGUE
        assertTrue(StatusOS.CONCLUIDA.podeTransicionarPara(StatusOS.ENTREGUE));

        // Mas não pode para outros status
        assertFalse(StatusOS.CONCLUIDA.podeTransicionarPara(StatusOS.ABERTA));
        assertFalse(StatusOS.CONCLUIDA.podeTransicionarPara(StatusOS.AGUARDANDO_ORCAMENTO));
        assertFalse(StatusOS.CONCLUIDA.podeTransicionarPara(StatusOS.ORCAMENTO_APROVADO));
        assertFalse(StatusOS.CONCLUIDA.podeTransicionarPara(StatusOS.EM_EXECUCAO));
        assertFalse(StatusOS.CONCLUIDA.podeTransicionarPara(StatusOS.CANCELADA));
        assertFalse(StatusOS.CONCLUIDA.podeTransicionarPara(StatusOS.CONCLUIDA));
    }

    @Test
    @DisplayName("Não deve permitir nenhuma transição de CANCELADA")
    void naoDevePermitirNenhumaTransicaoDeCancelada() {
        assertFalse(StatusOS.CANCELADA.podeTransicionarPara(StatusOS.ABERTA));
        assertFalse(StatusOS.CANCELADA.podeTransicionarPara(StatusOS.AGUARDANDO_ORCAMENTO));
        assertFalse(StatusOS.CANCELADA.podeTransicionarPara(StatusOS.ORCAMENTO_APROVADO));
        assertFalse(StatusOS.CANCELADA.podeTransicionarPara(StatusOS.EM_EXECUCAO));
        assertFalse(StatusOS.CANCELADA.podeTransicionarPara(StatusOS.CONCLUIDA));
        assertFalse(StatusOS.CANCELADA.podeTransicionarPara(StatusOS.CANCELADA));
    }

    @ParameterizedTest
    @MethodSource("transicoesValidasProvider")
    @DisplayName("Deve validar todas as transições válidas")
    void deveValidarTodasAsTransicoesValidas(StatusOS origem, StatusOS destino, boolean esperado) {
        assertEquals(esperado, origem.podeTransicionarPara(destino));
    }

    private static Stream<Arguments> transicoesValidasProvider() {
        return Stream.of(
                // Transições válidas - RECEBIDA/ABERTA primeiro para EM_DIAGNOSTICO
                Arguments.of(StatusOS.ABERTA, StatusOS.EM_DIAGNOSTICO, true),
                Arguments.of(StatusOS.ABERTA, StatusOS.CANCELADA, true),
                Arguments.of(StatusOS.EM_DIAGNOSTICO, StatusOS.AGUARDANDO_ORCAMENTO, true),
                Arguments.of(StatusOS.EM_DIAGNOSTICO, StatusOS.CANCELADA, true),
                Arguments.of(StatusOS.AGUARDANDO_ORCAMENTO, StatusOS.ORCAMENTO_APROVADO, true),
                Arguments.of(StatusOS.AGUARDANDO_ORCAMENTO, StatusOS.CANCELADA, true),
                Arguments.of(StatusOS.AGUARDANDO_ORCAMENTO, StatusOS.EM_DIAGNOSTICO, true), // Volta para diagnóstico
                Arguments.of(StatusOS.ORCAMENTO_APROVADO, StatusOS.EM_EXECUCAO, true),
                Arguments.of(StatusOS.ORCAMENTO_APROVADO, StatusOS.CANCELADA, true),
                Arguments.of(StatusOS.EM_EXECUCAO, StatusOS.CONCLUIDA, true),
                Arguments.of(StatusOS.EM_EXECUCAO, StatusOS.AGUARDANDO_ORCAMENTO, true), // Nova aprovação
                Arguments.of(StatusOS.CONCLUIDA, StatusOS.ENTREGUE, true),

                // Transições inválidas
                Arguments.of(StatusOS.ABERTA, StatusOS.AGUARDANDO_ORCAMENTO, false), // Precisa passar por
                                                                                     // EM_DIAGNOSTICO
                Arguments.of(StatusOS.ABERTA, StatusOS.ORCAMENTO_APROVADO, false),
                Arguments.of(StatusOS.ABERTA, StatusOS.EM_EXECUCAO, false),
                Arguments.of(StatusOS.CONCLUIDA, StatusOS.CANCELADA, false),
                Arguments.of(StatusOS.CANCELADA, StatusOS.ABERTA, false));
    }
}
