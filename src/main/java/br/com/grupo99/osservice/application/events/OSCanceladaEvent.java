package br.com.grupo99.osservice.application.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento de compensação - OS precisa ser cancelada/revertida
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OSCanceladaEvent {
    private UUID osId;
    private String motivo;
    private String etapaFalha; // BILLING, EXECUTION, PAYMENT
    private LocalDateTime timestamp;
    private String eventType = "OS_CANCELADA";
}
