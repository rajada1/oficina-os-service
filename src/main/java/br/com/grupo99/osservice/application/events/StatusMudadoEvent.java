package br.com.grupo99.osservice.application.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusMudadoEvent {
    private UUID osId;
    private String statusAnterior;
    private String statusNovo;
    private LocalDateTime timestamp;
    private String eventType = "STATUS_MUDADO";
}
