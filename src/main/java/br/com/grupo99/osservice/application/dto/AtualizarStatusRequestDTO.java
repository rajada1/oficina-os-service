package br.com.grupo99.osservice.application.dto;

import br.com.grupo99.osservice.domain.model.StatusOS;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atualização de status da OS
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarStatusRequestDTO {

    @NotNull(message = "Novo status é obrigatório")
    private StatusOS novoStatus;

    private String observacao;

    private String usuarioAlteracao;
}
