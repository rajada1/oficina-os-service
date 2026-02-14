package br.com.grupo99.osservice.application.dto;

import br.com.grupo99.osservice.domain.model.StatusOS;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de resposta para Ordem de Serviço
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServicoResponseDTO {

    private UUID id;
    private UUID clienteId;
    private UUID veiculoId;
    private StatusOS status;
    private String descricaoProblema;
    private BigDecimal valorTotal;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataCriacao;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataFinalizacao;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataEntrega;

    private List<HistoricoStatusDTO> historico;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoricoStatusDTO {
        private StatusOS statusAnterior;
        private StatusOS novoStatus;
        private String observacao;
        private String usuarioAlteracao;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime dataAlteracao;
    }
}
