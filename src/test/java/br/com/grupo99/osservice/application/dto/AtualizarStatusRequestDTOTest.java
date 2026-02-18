package br.com.grupo99.osservice.application.dto;

import br.com.grupo99.osservice.domain.model.StatusOS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AtualizarStatusRequestDTO - Testes Unitários")
class AtualizarStatusRequestDTOTest {

    @Test
    @DisplayName("Deve criar DTO com construtor padrão")
    void deveCriarDTOComConstrutorPadrao() {
        AtualizarStatusRequestDTO dto = new AtualizarStatusRequestDTO();

        assertThat(dto.getNovoStatus()).isNull();
        assertThat(dto.getObservacao()).isNull();
        assertThat(dto.getUsuarioAlteracao()).isNull();
    }

    @Test
    @DisplayName("Deve criar DTO com todos os argumentos")
    void deveCriarDTOComTodosOsArgumentos() {
        AtualizarStatusRequestDTO dto = new AtualizarStatusRequestDTO(
                StatusOS.EM_DIAGNOSTICO, "Iniciando diagnóstico", "mecanico1"
        );

        assertThat(dto.getNovoStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
        assertThat(dto.getObservacao()).isEqualTo("Iniciando diagnóstico");
        assertThat(dto.getUsuarioAlteracao()).isEqualTo("mecanico1");
    }

    @Test
    @DisplayName("Deve usar setters e getters")
    void deveUsarSettersEGetters() {
        AtualizarStatusRequestDTO dto = new AtualizarStatusRequestDTO();

        dto.setNovoStatus(StatusOS.FINALIZADA);
        dto.setObservacao("Serviço concluído");
        dto.setUsuarioAlteracao("admin");

        assertThat(dto.getNovoStatus()).isEqualTo(StatusOS.FINALIZADA);
        assertThat(dto.getObservacao()).isEqualTo("Serviço concluído");
        assertThat(dto.getUsuarioAlteracao()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Deve implementar equals e hashCode via @Data")
    void deveImplementarEqualsEHashCode() {
        AtualizarStatusRequestDTO dto1 = new AtualizarStatusRequestDTO(
                StatusOS.EM_DIAGNOSTICO, "Obs", "user"
        );
        AtualizarStatusRequestDTO dto2 = new AtualizarStatusRequestDTO(
                StatusOS.EM_DIAGNOSTICO, "Obs", "user"
        );

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    @DisplayName("Deve ter toString não nulo")
    void deveImplementarToString() {
        AtualizarStatusRequestDTO dto = new AtualizarStatusRequestDTO(
                StatusOS.EM_DIAGNOSTICO, "Obs", "user"
        );

        assertThat(dto.toString()).isNotNull();
    }
}
