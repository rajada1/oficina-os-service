package br.com.grupo99.osservice.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrdemServicoRequestDTO - Testes Unitários")
class OrdemServicoRequestDTOTest {

    @Test
    @DisplayName("Deve criar DTO com construtor padrão")
    void deveCriarDTOComConstrutorPadrao() {
        OrdemServicoRequestDTO dto = new OrdemServicoRequestDTO();

        assertThat(dto.getClienteId()).isNull();
        assertThat(dto.getVeiculoId()).isNull();
        assertThat(dto.getDescricaoProblema()).isNull();
    }

    @Test
    @DisplayName("Deve criar DTO com todos os argumentos")
    void deveCriarDTOComTodosOsArgumentos() {
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();

        OrdemServicoRequestDTO dto = new OrdemServicoRequestDTO(clienteId, veiculoId, "Problema no motor");

        assertThat(dto.getClienteId()).isEqualTo(clienteId);
        assertThat(dto.getVeiculoId()).isEqualTo(veiculoId);
        assertThat(dto.getDescricaoProblema()).isEqualTo("Problema no motor");
    }

    @Test
    @DisplayName("Deve usar setters e getters")
    void deveUsarSettersEGetters() {
        OrdemServicoRequestDTO dto = new OrdemServicoRequestDTO();
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();

        dto.setClienteId(clienteId);
        dto.setVeiculoId(veiculoId);
        dto.setDescricaoProblema("Freio com barulho");

        assertThat(dto.getClienteId()).isEqualTo(clienteId);
        assertThat(dto.getVeiculoId()).isEqualTo(veiculoId);
        assertThat(dto.getDescricaoProblema()).isEqualTo("Freio com barulho");
    }

    @Test
    @DisplayName("Deve implementar equals e hashCode via @Data")
    void deveImplementarEqualsEHashCode() {
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();

        OrdemServicoRequestDTO dto1 = new OrdemServicoRequestDTO(clienteId, veiculoId, "Problema");
        OrdemServicoRequestDTO dto2 = new OrdemServicoRequestDTO(clienteId, veiculoId, "Problema");

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    @DisplayName("Deve ter toString não nulo")
    void deveImplementarToString() {
        OrdemServicoRequestDTO dto = new OrdemServicoRequestDTO(UUID.randomUUID(), UUID.randomUUID(), "Desc");

        assertThat(dto.toString()).isNotNull();
    }
}
