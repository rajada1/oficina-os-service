package br.com.grupo99.osservice.application.dto;

import br.com.grupo99.osservice.domain.model.StatusOS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DTOs da aplicação")
class DtoTest {

    @Test
    @DisplayName("OrdemServicoRequestDTO - deve criar com todos os campos")
    void ordemServicoRequestDTO_deveCriarComTodosCampos() {
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        String descricao = "Troca de óleo";

        OrdemServicoRequestDTO dto = new OrdemServicoRequestDTO(clienteId, veiculoId, descricao);

        assertThat(dto.getClienteId()).isEqualTo(clienteId);
        assertThat(dto.getVeiculoId()).isEqualTo(veiculoId);
        assertThat(dto.getDescricaoProblema()).isEqualTo(descricao);
    }

    @Test
    @DisplayName("OrdemServicoRequestDTO - deve criar vazio e usar setters")
    void ordemServicoRequestDTO_deveCriarVazio() {
        OrdemServicoRequestDTO dto = new OrdemServicoRequestDTO();
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();

        dto.setClienteId(clienteId);
        dto.setVeiculoId(veiculoId);
        dto.setDescricaoProblema("Freio barulhento");

        assertThat(dto.getClienteId()).isEqualTo(clienteId);
        assertThat(dto.getVeiculoId()).isEqualTo(veiculoId);
        assertThat(dto.getDescricaoProblema()).isEqualTo("Freio barulhento");
    }

    @Test
    @DisplayName("OrdemServicoRequestDTO - equals e hashCode")
    void ordemServicoRequestDTO_equalsHashCode() {
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();

        OrdemServicoRequestDTO dto1 = new OrdemServicoRequestDTO(clienteId, veiculoId, "desc");
        OrdemServicoRequestDTO dto2 = new OrdemServicoRequestDTO(clienteId, veiculoId, "desc");

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.toString()).contains("desc");
    }

    @Test
    @DisplayName("AtualizarStatusRequestDTO - deve criar com todos os campos")
    void atualizarStatusRequestDTO_deveCriarComTodosCampos() {
        AtualizarStatusRequestDTO dto = new AtualizarStatusRequestDTO(
                StatusOS.EM_DIAGNOSTICO, "Iniciando diagnóstico", "mecanico01");

        assertThat(dto.getNovoStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
        assertThat(dto.getObservacao()).isEqualTo("Iniciando diagnóstico");
        assertThat(dto.getUsuarioAlteracao()).isEqualTo("mecanico01");
    }

    @Test
    @DisplayName("AtualizarStatusRequestDTO - deve criar vazio e usar setters")
    void atualizarStatusRequestDTO_deveCriarVazio() {
        AtualizarStatusRequestDTO dto = new AtualizarStatusRequestDTO();
        dto.setNovoStatus(StatusOS.FINALIZADA);
        dto.setObservacao("Concluído");
        dto.setUsuarioAlteracao("admin");

        assertThat(dto.getNovoStatus()).isEqualTo(StatusOS.FINALIZADA);
        assertThat(dto.getObservacao()).isEqualTo("Concluído");
        assertThat(dto.getUsuarioAlteracao()).isEqualTo("admin");
    }

    @Test
    @DisplayName("AtualizarStatusRequestDTO - equals e hashCode")
    void atualizarStatusRequestDTO_equalsHashCode() {
        AtualizarStatusRequestDTO dto1 = new AtualizarStatusRequestDTO(StatusOS.FINALIZADA, "obs", "user");
        AtualizarStatusRequestDTO dto2 = new AtualizarStatusRequestDTO(StatusOS.FINALIZADA, "obs", "user");

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.toString()).contains("FINALIZADA");
    }

    @Test
    @DisplayName("OrdemServicoResponseDTO - deve criar com todos os campos")
    void ordemServicoResponseDTO_deveCriarComTodosCampos() {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        OrdemServicoResponseDTO.HistoricoStatusDTO historico = new OrdemServicoResponseDTO.HistoricoStatusDTO(
                StatusOS.RECEBIDA, StatusOS.EM_DIAGNOSTICO, "Diagnóstico", "mecanico", now);

        OrdemServicoResponseDTO dto = new OrdemServicoResponseDTO(
                id, clienteId, veiculoId, StatusOS.EM_DIAGNOSTICO,
                "Problema no motor", BigDecimal.valueOf(1500),
                now, null, null, List.of(historico));

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getClienteId()).isEqualTo(clienteId);
        assertThat(dto.getVeiculoId()).isEqualTo(veiculoId);
        assertThat(dto.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
        assertThat(dto.getDescricaoProblema()).isEqualTo("Problema no motor");
        assertThat(dto.getValorTotal()).isEqualByComparingTo(BigDecimal.valueOf(1500));
        assertThat(dto.getDataCriacao()).isEqualTo(now);
        assertThat(dto.getDataFinalizacao()).isNull();
        assertThat(dto.getDataEntrega()).isNull();
        assertThat(dto.getHistorico()).hasSize(1);
    }

    @Test
    @DisplayName("OrdemServicoResponseDTO - deve criar vazio e usar setters")
    void ordemServicoResponseDTO_deveCriarVazio() {
        OrdemServicoResponseDTO dto = new OrdemServicoResponseDTO();
        UUID id = UUID.randomUUID();
        dto.setId(id);
        dto.setStatus(StatusOS.RECEBIDA);
        dto.setValorTotal(BigDecimal.ZERO);
        dto.setHistorico(List.of());

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getStatus()).isEqualTo(StatusOS.RECEBIDA);
        assertThat(dto.getHistorico()).isEmpty();
    }

    @Test
    @DisplayName("OrdemServicoResponseDTO - equals e hashCode")
    void ordemServicoResponseDTO_equalsHashCode() {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        OrdemServicoResponseDTO dto1 = new OrdemServicoResponseDTO(
                id, clienteId, veiculoId, StatusOS.RECEBIDA, "desc",
                BigDecimal.ZERO, now, null, null, List.of());
        OrdemServicoResponseDTO dto2 = new OrdemServicoResponseDTO(
                id, clienteId, veiculoId, StatusOS.RECEBIDA, "desc",
                BigDecimal.ZERO, now, null, null, List.of());

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    @DisplayName("HistoricoStatusDTO - deve criar vazio e usar setters")
    void historicoStatusDTO_deveCriarVazio() {
        OrdemServicoResponseDTO.HistoricoStatusDTO dto = new OrdemServicoResponseDTO.HistoricoStatusDTO();
        dto.setStatusAnterior(StatusOS.RECEBIDA);
        dto.setNovoStatus(StatusOS.EM_DIAGNOSTICO);
        dto.setObservacao("obs");
        dto.setUsuarioAlteracao("user");
        dto.setDataAlteracao(LocalDateTime.now());

        assertThat(dto.getStatusAnterior()).isEqualTo(StatusOS.RECEBIDA);
        assertThat(dto.getNovoStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
        assertThat(dto.getObservacao()).isEqualTo("obs");
        assertThat(dto.getUsuarioAlteracao()).isEqualTo("user");
        assertThat(dto.getDataAlteracao()).isNotNull();
    }

    @Test
    @DisplayName("HistoricoStatusDTO - equals, hashCode e toString")
    void historicoStatusDTO_equalsHashCodeToString() {
        LocalDateTime now = LocalDateTime.now();
        OrdemServicoResponseDTO.HistoricoStatusDTO dto1 = new OrdemServicoResponseDTO.HistoricoStatusDTO(
                StatusOS.RECEBIDA, StatusOS.EM_DIAGNOSTICO, "obs", "user", now);
        OrdemServicoResponseDTO.HistoricoStatusDTO dto2 = new OrdemServicoResponseDTO.HistoricoStatusDTO(
                StatusOS.RECEBIDA, StatusOS.EM_DIAGNOSTICO, "obs", "user", now);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.toString()).contains("RECEBIDA");
    }
}
