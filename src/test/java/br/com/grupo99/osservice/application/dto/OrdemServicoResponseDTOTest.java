package br.com.grupo99.osservice.application.dto;

import br.com.grupo99.osservice.domain.model.StatusOS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrdemServicoResponseDTO - Testes Unitários")
class OrdemServicoResponseDTOTest {

    @Test
    @DisplayName("Deve criar DTO com construtor padrão")
    void deveCriarDTOComConstrutorPadrao() {
        OrdemServicoResponseDTO dto = new OrdemServicoResponseDTO();

        assertThat(dto.getId()).isNull();
        assertThat(dto.getClienteId()).isNull();
        assertThat(dto.getVeiculoId()).isNull();
        assertThat(dto.getStatus()).isNull();
        assertThat(dto.getDescricaoProblema()).isNull();
        assertThat(dto.getValorTotal()).isNull();
        assertThat(dto.getDataCriacao()).isNull();
        assertThat(dto.getDataFinalizacao()).isNull();
        assertThat(dto.getDataEntrega()).isNull();
        assertThat(dto.getHistorico()).isNull();
    }

    @Test
    @DisplayName("Deve criar DTO com todos os argumentos")
    void deveCriarDTOComTodosOsArgumentos() {
        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        OrdemServicoResponseDTO.HistoricoStatusDTO historico = new OrdemServicoResponseDTO.HistoricoStatusDTO(
                StatusOS.RECEBIDA, StatusOS.EM_DIAGNOSTICO, "Obs", "user", agora
        );

        OrdemServicoResponseDTO dto = new OrdemServicoResponseDTO(
                id, clienteId, veiculoId, StatusOS.EM_DIAGNOSTICO,
                "Problema", BigDecimal.TEN, agora, null, null,
                List.of(historico)
        );

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getClienteId()).isEqualTo(clienteId);
        assertThat(dto.getVeiculoId()).isEqualTo(veiculoId);
        assertThat(dto.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
        assertThat(dto.getDescricaoProblema()).isEqualTo("Problema");
        assertThat(dto.getValorTotal()).isEqualTo(BigDecimal.TEN);
        assertThat(dto.getHistorico()).hasSize(1);
    }

    @Test
    @DisplayName("Deve usar setters e getters")
    void deveUsarSettersEGetters() {
        OrdemServicoResponseDTO dto = new OrdemServicoResponseDTO();
        UUID id = UUID.randomUUID();
        LocalDateTime agora = LocalDateTime.now();

        dto.setId(id);
        dto.setClienteId(UUID.randomUUID());
        dto.setVeiculoId(UUID.randomUUID());
        dto.setStatus(StatusOS.FINALIZADA);
        dto.setDescricaoProblema("Desc");
        dto.setValorTotal(BigDecimal.valueOf(150.50));
        dto.setDataCriacao(agora);
        dto.setDataFinalizacao(agora);
        dto.setDataEntrega(agora);
        dto.setHistorico(List.of());

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getStatus()).isEqualTo(StatusOS.FINALIZADA);
        assertThat(dto.getValorTotal()).isEqualByComparingTo(BigDecimal.valueOf(150.50));
    }

    @Test
    @DisplayName("Deve criar HistoricoStatusDTO com construtor padrão")
    void deveCriarHistoricoDTOComConstrutorPadrao() {
        OrdemServicoResponseDTO.HistoricoStatusDTO dto = new OrdemServicoResponseDTO.HistoricoStatusDTO();

        assertThat(dto.getStatusAnterior()).isNull();
        assertThat(dto.getNovoStatus()).isNull();
        assertThat(dto.getObservacao()).isNull();
        assertThat(dto.getUsuarioAlteracao()).isNull();
        assertThat(dto.getDataAlteracao()).isNull();
    }

    @Test
    @DisplayName("Deve criar HistoricoStatusDTO com todos os argumentos")
    void deveCriarHistoricoDTOComTodosOsArgumentos() {
        LocalDateTime agora = LocalDateTime.now();
        OrdemServicoResponseDTO.HistoricoStatusDTO dto = new OrdemServicoResponseDTO.HistoricoStatusDTO(
                StatusOS.RECEBIDA, StatusOS.EM_DIAGNOSTICO, "Obs", "user", agora
        );

        assertThat(dto.getStatusAnterior()).isEqualTo(StatusOS.RECEBIDA);
        assertThat(dto.getNovoStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
        assertThat(dto.getObservacao()).isEqualTo("Obs");
        assertThat(dto.getUsuarioAlteracao()).isEqualTo("user");
        assertThat(dto.getDataAlteracao()).isEqualTo(agora);
    }

    @Test
    @DisplayName("Deve usar setters no HistoricoStatusDTO")
    void deveUsarSettersNoHistoricoDTO() {
        OrdemServicoResponseDTO.HistoricoStatusDTO dto = new OrdemServicoResponseDTO.HistoricoStatusDTO();
        LocalDateTime agora = LocalDateTime.now();

        dto.setStatusAnterior(StatusOS.RECEBIDA);
        dto.setNovoStatus(StatusOS.CANCELADA);
        dto.setObservacao("Cancelado");
        dto.setUsuarioAlteracao("admin");
        dto.setDataAlteracao(agora);

        assertThat(dto.getStatusAnterior()).isEqualTo(StatusOS.RECEBIDA);
        assertThat(dto.getNovoStatus()).isEqualTo(StatusOS.CANCELADA);
        assertThat(dto.getObservacao()).isEqualTo("Cancelado");
        assertThat(dto.getUsuarioAlteracao()).isEqualTo("admin");
        assertThat(dto.getDataAlteracao()).isEqualTo(agora);
    }
}
