package br.com.grupo99.osservice.bdd;

import br.com.grupo99.osservice.domain.model.OrdemServico;
import br.com.grupo99.osservice.domain.model.StatusOS;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.cucumber.datatable.DataTable;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class OrdemServicoSteps {

    private OrdemServico ordemServico;
    private Exception exception;

    @Quando("eu criar uma ordem de serviço com os dados:")
    public void euCriarUmaOrdemDeServicoComOsDados(DataTable dataTable) {
        Map<String, String> dados = dataTable.asMap(String.class, String.class);

        ordemServico = OrdemServico.criar(
                dados.get("clienteNome"),
                dados.get("clienteEmail"),
                dados.get("clienteTelefone"),
                dados.get("veiculoPlaca"),
                dados.get("descricao"));
    }

    @Então("a ordem de serviço deve ser criada com sucesso")
    public void aOrdemDeServicoDeveSerCriadaComSucesso() {
        assertNotNull(ordemServico);
        assertNotNull(ordemServico.getId());
    }

    @Então("o status deve ser {string}")
    public void oStatusDeveSer(String status) {
        assertEquals(StatusOS.valueOf(status), ordemServico.getStatus());
    }

    @Dado("que existe uma ordem de serviço criada")
    public void queExisteUmaOrdemDeServicoCriada() {
        ordemServico = OrdemServico.criar(
                "Cliente Teste",
                "cliente@test.com",
                "(11) 99999-9999",
                "TEST-1234",
                "Descrição de teste");
        assertNotNull(ordemServico);
    }

    @Quando("a ordem é enviada para aguardar orçamento")
    public void aOrdemEEnviadaParaAguardarOrcamento() {
        ordemServico.aguardarOrcamento();
    }

    @Quando("o orçamento é aprovado pelo cliente")
    public void oOrcamentoEAprovadoPeloCliente() {
        ordemServico.aprovarOrcamento();
    }

    @Quando("a execução é iniciada")
    public void aExecucaoEIniciada() {
        ordemServico.iniciarExecucao();
    }

    @Quando("o serviço é concluído")
    public void oServicoEConcluido() {
        ordemServico.concluir();
    }

    @Então("a data de conclusão deve estar preenchida")
    public void aDataDeConclusaoDeveEstarPreenchida() {
        assertNotNull(ordemServico.getDataConclusao());
    }

    @Quando("a ordem é cancelada")
    public void aOrdemECancelada() {
        ordemServico.cancelar();
    }

    @Quando("eu tentar aprovar o orçamento sem aguardar")
    public void euTentarAprovarOOrcamentoSemAguardar() {
        try {
            ordemServico.aprovarOrcamento();
        } catch (IllegalStateException e) {
            exception = e;
        }
    }

    @Então("deve ocorrer um erro de transição inválida")
    public void deveOcorrerUmErroDeTransicaoInvalida() {
        assertNotNull(exception);
        assertTrue(exception instanceof IllegalStateException);
    }

    @Quando("a ordem passa por várias transições de status")
    public void aOrdemPassaPorVariasTransicoesDeStatus() {
        ordemServico.aguardarOrcamento();
        ordemServico.aprovarOrcamento();
        ordemServico.iniciarExecucao();
    }

    @Então("o histórico deve registrar todas as mudanças")
    public void oHistoricoDeveRegistrarTodasAsMudancas() {
        assertEquals(3, ordemServico.getHistorico().size());
    }

    @Então("cada mudança deve ter data e status anterior e novo")
    public void cadaMudancaDeveTerDataEStatusAnteriorENovo() {
        ordemServico.getHistorico().forEach(hist -> {
            assertNotNull(hist.getStatusAnterior());
            assertNotNull(hist.getStatusNovo());
            assertNotNull(hist.getDataTransicao());
        });
    }
}
