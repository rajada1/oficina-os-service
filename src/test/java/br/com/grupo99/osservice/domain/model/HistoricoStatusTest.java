package br.com.grupo99.osservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HistoricoStatus - Testes Unitários")
class HistoricoStatusTest {

    @Test
    @DisplayName("Deve criar HistoricoStatus com construtor de negócio")
    void deveCriarHistoricoComConstrutorDeNegocio() {
        OrdemServico os = new OrdemServico(UUID.randomUUID(), UUID.randomUUID(), "Problema teste");

        HistoricoStatus historico = new HistoricoStatus(
                os,
                StatusOS.RECEBIDA,
                StatusOS.EM_DIAGNOSTICO,
                "Iniciando diagnóstico",
                "mecânico1"
        );

        assertThat(historico.getOrdemServico()).isEqualTo(os);
        assertThat(historico.getStatusAnterior()).isEqualTo(StatusOS.RECEBIDA);
        assertThat(historico.getNovoStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
        assertThat(historico.getObservacao()).isEqualTo("Iniciando diagnóstico");
        assertThat(historico.getUsuarioAlteracao()).isEqualTo("mecânico1");
        assertThat(historico.getDataAlteracao()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar HistoricoStatus com construtor padrão")
    void deveCriarHistoricoComConstrutorPadrao() {
        HistoricoStatus historico = new HistoricoStatus();

        assertThat(historico.getId()).isNull();
        assertThat(historico.getOrdemServico()).isNull();
        assertThat(historico.getStatusAnterior()).isNull();
        assertThat(historico.getNovoStatus()).isNull();
        assertThat(historico.getObservacao()).isNull();
    }

    @Test
    @DisplayName("Deve criar HistoricoStatus com todos os argumentos")
    void deveCriarHistoricoComTodosOsArgumentos() {
        UUID id = UUID.randomUUID();
        OrdemServico os = new OrdemServico(UUID.randomUUID(), UUID.randomUUID(), "Problema");
        LocalDateTime dataAlteracao = LocalDateTime.now();

        HistoricoStatus historico = new HistoricoStatus(
                id, os, StatusOS.RECEBIDA, StatusOS.EM_DIAGNOSTICO,
                "Obs", "usuario1", dataAlteracao
        );

        assertThat(historico.getId()).isEqualTo(id);
        assertThat(historico.getOrdemServico()).isEqualTo(os);
        assertThat(historico.getStatusAnterior()).isEqualTo(StatusOS.RECEBIDA);
        assertThat(historico.getNovoStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
        assertThat(historico.getObservacao()).isEqualTo("Obs");
        assertThat(historico.getUsuarioAlteracao()).isEqualTo("usuario1");
        assertThat(historico.getDataAlteracao()).isEqualTo(dataAlteracao);
    }

    @Test
    @DisplayName("Deve retornar statusNovo via método de compatibilidade")
    void deveRetornarStatusNovoViaMetodoDeCompatibilidade() {
        OrdemServico os = new OrdemServico(UUID.randomUUID(), UUID.randomUUID(), "Problema");
        HistoricoStatus historico = new HistoricoStatus(
                os, StatusOS.RECEBIDA, StatusOS.EM_DIAGNOSTICO, "Obs", "user"
        );

        assertThat(historico.getStatusNovo()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
    }

    @Test
    @DisplayName("Deve retornar dataTransicao via método de compatibilidade")
    void deveRetornarDataTransicaoViaMetodoDeCompatibilidade() {
        OrdemServico os = new OrdemServico(UUID.randomUUID(), UUID.randomUUID(), "Problema");
        HistoricoStatus historico = new HistoricoStatus(
                os, StatusOS.RECEBIDA, StatusOS.EM_DIAGNOSTICO, "Obs", "user"
        );

        assertThat(historico.getDataTransicao()).isNotNull();
        assertThat(historico.getDataTransicao()).isEqualTo(historico.getDataAlteracao());
    }

    @Test
    @DisplayName("Deve preencher dataAlteracao no onCreate se nula")
    void devePreencherDataAlteracaoNoOnCreateSeNula() {
        HistoricoStatus historico = new HistoricoStatus();
        historico.setDataAlteracao(null);

        historico.onCreate();

        assertThat(historico.getDataAlteracao()).isNotNull();
    }

    @Test
    @DisplayName("Não deve alterar dataAlteracao no onCreate se já preenchida")
    void naoDeveAlterarDataAlteracaoNoOnCreateSeJaPreenchida() {
        HistoricoStatus historico = new HistoricoStatus();
        LocalDateTime dataOriginal = LocalDateTime.of(2024, 1, 1, 10, 0);
        historico.setDataAlteracao(dataOriginal);

        historico.onCreate();

        assertThat(historico.getDataAlteracao()).isEqualTo(dataOriginal);
    }

    @Test
    @DisplayName("Deve comparar igualdade por ID")
    void deveCompararIgualdadePorId() {
        UUID id = UUID.randomUUID();
        HistoricoStatus h1 = new HistoricoStatus();
        h1.setId(id);
        HistoricoStatus h2 = new HistoricoStatus();
        h2.setId(id);

        assertThat(h1).isEqualTo(h2);
        assertThat(h1.hashCode()).isEqualTo(h2.hashCode());
    }

    @Test
    @DisplayName("Deve retornar false para igualdade com IDs diferentes")
    void deveRetornarFalseParaIgualdadeComIdsDiferentes() {
        HistoricoStatus h1 = new HistoricoStatus();
        h1.setId(UUID.randomUUID());
        HistoricoStatus h2 = new HistoricoStatus();
        h2.setId(UUID.randomUUID());

        assertThat(h1).isNotEqualTo(h2);
    }

    @Test
    @DisplayName("Deve retornar false para igualdade com null")
    void deveRetornarFalseParaIgualdadeComNull() {
        HistoricoStatus h1 = new HistoricoStatus();
        h1.setId(UUID.randomUUID());

        assertThat(h1).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Deve retornar false para igualdade com tipo diferente")
    void deveRetornarFalseParaIgualdadeComTipoDiferente() {
        HistoricoStatus h1 = new HistoricoStatus();
        h1.setId(UUID.randomUUID());

        assertThat(h1).isNotEqualTo("string");
    }

    @Test
    @DisplayName("Deve retornar true para igualdade consigo mesmo")
    void deveRetornarTrueParaIgualdadeConsigoMesmo() {
        HistoricoStatus h1 = new HistoricoStatus();
        h1.setId(UUID.randomUUID());

        assertThat(h1).isEqualTo(h1);
    }

    @Test
    @DisplayName("Deve gerar toString com informações relevantes")
    void deveGerarToStringComInformacoesRelevantes() {
        UUID id = UUID.randomUUID();
        HistoricoStatus historico = new HistoricoStatus();
        historico.setId(id);
        historico.setStatusAnterior(StatusOS.RECEBIDA);
        historico.setNovoStatus(StatusOS.EM_DIAGNOSTICO);
        historico.setDataAlteracao(LocalDateTime.now());

        String toString = historico.toString();
        assertThat(toString).contains("HistoricoStatus");
        assertThat(toString).contains(id.toString());
        assertThat(toString).contains("RECEBIDA");
        assertThat(toString).contains("EM_DIAGNOSTICO");
    }

    @Test
    @DisplayName("Deve permitir status anterior nulo (criação inicial)")
    void devePermitirStatusAnteriorNulo() {
        OrdemServico os = new OrdemServico(UUID.randomUUID(), UUID.randomUUID(), "Problema");
        HistoricoStatus historico = new HistoricoStatus(
                os, null, StatusOS.RECEBIDA, "OS criada", "Sistema"
        );

        assertThat(historico.getStatusAnterior()).isNull();
        assertThat(historico.getNovoStatus()).isEqualTo(StatusOS.RECEBIDA);
    }

    @Test
    @DisplayName("Deve usar setters corretamente")
    void deveUsarSettersCorretamente() {
        HistoricoStatus historico = new HistoricoStatus();
        UUID id = UUID.randomUUID();
        OrdemServico os = new OrdemServico(UUID.randomUUID(), UUID.randomUUID(), "Teste");
        LocalDateTime agora = LocalDateTime.now();

        historico.setId(id);
        historico.setOrdemServico(os);
        historico.setStatusAnterior(StatusOS.RECEBIDA);
        historico.setNovoStatus(StatusOS.EM_DIAGNOSTICO);
        historico.setObservacao("Observação");
        historico.setUsuarioAlteracao("admin");
        historico.setDataAlteracao(agora);

        assertThat(historico.getId()).isEqualTo(id);
        assertThat(historico.getOrdemServico()).isEqualTo(os);
        assertThat(historico.getStatusAnterior()).isEqualTo(StatusOS.RECEBIDA);
        assertThat(historico.getNovoStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
        assertThat(historico.getObservacao()).isEqualTo("Observação");
        assertThat(historico.getUsuarioAlteracao()).isEqualTo("admin");
        assertThat(historico.getDataAlteracao()).isEqualTo(agora);
    }
}
