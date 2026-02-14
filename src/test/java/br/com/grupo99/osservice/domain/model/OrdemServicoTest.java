package br.com.grupo99.osservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrdemServico - Testes Unitários")
class OrdemServicoTest {

    @Test
    @DisplayName("Deve criar ordem de serviço com status RECEBIDA")
    void deveCriarOrdemServicoComStatusRecebida() {
        // Arrange & Act
        OrdemServico os = OrdemServico.criar(
                "João Silva",
                "joao@email.com",
                "(11) 99999-9999",
                "ABC-1234",
                "Problema no motor");

        // Assert
        assertNotNull(os);
        assertNotNull(os.getId());
        assertEquals(StatusOS.RECEBIDA, os.getStatus());
        assertEquals("João Silva", os.getClienteNome());
        assertEquals("joao@email.com", os.getClienteEmail());
        assertEquals("ABC-1234", os.getVeiculoPlaca());
        assertNotNull(os.getDataAbertura());
        assertNull(os.getDataConclusao());
    }

    @Test
    @DisplayName("Deve adicionar histórico ao transicionar status")
    void deveAdicionarHistoricoAoTransicionarStatus() {
        // Arrange
        OrdemServico os = OrdemServico.criar(
                "Maria Santos",
                "maria@email.com",
                "(11) 88888-8888",
                "XYZ-5678",
                "Revisão periódica");

        // Act
        os.aguardarOrcamento();

        // Assert
        assertEquals(StatusOS.AGUARDANDO_APROVACAO, os.getStatus());
        assertTrue(os.getHistorico().size() >= 2);

        HistoricoStatus hist = os.getHistorico().get(os.getHistorico().size() - 1);
        assertEquals(StatusOS.EM_DIAGNOSTICO, hist.getStatusAnterior());
        assertEquals(StatusOS.AGUARDANDO_APROVACAO, hist.getStatusNovo());
        assertNotNull(hist.getDataTransicao());
    }

    @Test
    @DisplayName("Deve transicionar de RECEBIDA para AGUARDANDO_APROVACAO")
    void deveTransicionarDeRecebidaParaAguardandoAprovacao() {
        // Arrange
        OrdemServico os = OrdemServico.criar("Cliente", "email@test.com", "11999999999", "AAA-1111", "Desc");

        // Act
        os.aguardarOrcamento();

        // Assert
        assertEquals(StatusOS.AGUARDANDO_APROVACAO, os.getStatus());
    }

    @Test
    @DisplayName("Deve transicionar de AGUARDANDO_APROVACAO para AGUARDANDO_PAGAMENTO")
    void deveTransicionarDeAguardandoAprovacaoParaAguardandoPagamento() {
        // Arrange
        OrdemServico os = OrdemServico.criar("Cliente", "email@test.com", "11999999999", "BBB-2222", "Desc");
        os.aguardarOrcamento();

        // Act
        os.aprovarOrcamento();

        // Assert
        assertEquals(StatusOS.AGUARDANDO_PAGAMENTO, os.getStatus());
        assertTrue(os.getHistorico().size() >= 3);
    }

    @Test
    @DisplayName("Deve transicionar de AGUARDANDO_PAGAMENTO para EM_EXECUCAO")
    void deveTransicionarDeAguardandoPagamentoParaEmExecucao() {
        // Arrange
        OrdemServico os = OrdemServico.criar("Cliente", "email@test.com", "11999999999", "CCC-3333", "Desc");
        os.aguardarOrcamento();
        os.aprovarOrcamento();

        // Act
        os.iniciarExecucao();

        // Assert
        assertEquals(StatusOS.EM_EXECUCAO, os.getStatus());
        assertTrue(os.getHistorico().size() >= 4);
    }

    @Test
    @DisplayName("Deve transicionar de EM_EXECUCAO para FINALIZADA")
    void deveTransicionarDeEmExecucaoParaFinalizada() {
        // Arrange
        OrdemServico os = OrdemServico.criar("Cliente", "email@test.com", "11999999999", "DDD-4444", "Desc");
        os.aguardarOrcamento();
        os.aprovarOrcamento();
        os.iniciarExecucao();

        // Act
        os.concluir();

        // Assert
        assertEquals(StatusOS.FINALIZADA, os.getStatus());
        assertNotNull(os.getDataConclusao());
        assertTrue(os.getHistorico().size() >= 5);
    }

    @Test
    @DisplayName("Deve cancelar ordem de serviço em qualquer status")
    void deveCancelarOrdemServicoEmQualquerStatus() {
        // Arrange
        OrdemServico os = OrdemServico.criar("Cliente", "email@test.com", "11999999999", "EEE-5555", "Desc");
        os.aguardarOrcamento();

        // Act
        os.cancelar();

        // Assert
        assertEquals(StatusOS.CANCELADA, os.getStatus());
        assertTrue(os.getHistorico().size() >= 3);
    }

    @Test
    @DisplayName("Não deve transicionar para status inválido")
    void naoDeveTransicionarParaStatusInvalido() {
        // Arrange
        OrdemServico os = OrdemServico.criar("Cliente", "email@test.com", "11999999999", "FFF-6666", "Desc");

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> os.aprovarOrcamento());
        assertThrows(IllegalStateException.class, () -> os.iniciarExecucao());
        assertThrows(IllegalStateException.class, () -> os.concluir());
    }

    @Test
    @DisplayName("Não deve permitir transição de FINALIZADA")
    void naoDevePermitirTransicaoDeFinalizada() {
        // Arrange
        OrdemServico os = OrdemServico.criar("Cliente", "email@test.com", "11999999999", "GGG-7777", "Desc");
        os.aguardarOrcamento();
        os.aprovarOrcamento();
        os.iniciarExecucao();
        os.concluir();

        // Act & Assert
        // FINALIZADA só pode transicionar para ENTREGUE
        assertThrows(IllegalStateException.class, () -> os.aguardarOrcamento());
        assertThrows(IllegalStateException.class, () -> os.iniciarExecucao());
        assertThrows(IllegalStateException.class, () -> os.aprovarOrcamento());
    }

    @Test
    @DisplayName("Não deve permitir transição de CANCELADA")
    void naoDevePermitirTransicaoDeCancelada() {
        // Arrange
        OrdemServico os = OrdemServico.criar("Cliente", "email@test.com", "11999999999", "HHH-8888", "Desc");
        os.cancelar();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> os.aguardarOrcamento());
        assertThrows(IllegalStateException.class, () -> os.aprovarOrcamento());
    }

    @Test
    @DisplayName("Deve validar campos obrigatórios na criação")
    void deveValidarCamposObrigatoriosNaCriacao() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> OrdemServico.criar(null, "email@test.com", "11999999999", "III-9999", "Desc"));

        assertThrows(IllegalArgumentException.class,
                () -> OrdemServico.criar("", "email@test.com", "11999999999", "III-9999", "Desc"));

        assertThrows(IllegalArgumentException.class,
                () -> OrdemServico.criar("Cliente", null, "11999999999", "III-9999", "Desc"));

        assertThrows(IllegalArgumentException.class,
                () -> OrdemServico.criar("Cliente", "email@test.com", null, "III-9999", "Desc"));

        assertThrows(IllegalArgumentException.class,
                () -> OrdemServico.criar("Cliente", "email@test.com", "11999999999", null, "Desc"));

        assertThrows(IllegalArgumentException.class,
                () -> OrdemServico.criar("Cliente", "email@test.com", "11999999999", "III-9999", null));
    }

}
