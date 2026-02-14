package br.com.grupo99.osservice.application.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrcamentoAprovadoEvent {
    private UUID orcamentoId;
    private UUID osId;
    private LocalDateTime timestamp;
    private String eventType = "ORCAMENTO_APROVADO";
}
