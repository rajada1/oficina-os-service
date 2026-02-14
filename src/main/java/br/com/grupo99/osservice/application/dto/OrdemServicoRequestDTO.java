package br.com.grupo99.osservice.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de requisição para criar Ordem de Serviço
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServicoRequestDTO {

    @NotNull(message = "ClienteId é obrigatório")
    private UUID clienteId;

    @NotNull(message = "VeiculoId é obrigatório")
    private UUID veiculoId;

    private String descricaoProblema;
}
