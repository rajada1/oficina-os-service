package br.com.grupo99.osservice.application.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StatusMudadoEvent - Testes Unitários")
class StatusMudadoEventTest {

    @Test
    @DisplayName("Deve criar evento com construtor padrão")
    void deveCriarEventoComConstrutorPadrao() {
        StatusMudadoEvent event = new StatusMudadoEvent();

        assertThat(event.getOsId()).isNull();
        assertThat(event.getStatusAnterior()).isNull();
        assertThat(event.getStatusNovo()).isNull();
        assertThat(event.getTimestamp()).isNull();
        assertThat(event.getEventType()).isEqualTo("STATUS_MUDADO");
    }

    @Test
    @DisplayName("Deve criar evento com todos os argumentos")
    void deveCriarEventoComTodosOsArgumentos() {
        UUID osId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();

        StatusMudadoEvent event = new StatusMudadoEvent(osId, "RECEBIDA", "EM_DIAGNOSTICO", timestamp, "STATUS_MUDADO");

        assertThat(event.getOsId()).isEqualTo(osId);
        assertThat(event.getStatusAnterior()).isEqualTo("RECEBIDA");
        assertThat(event.getStatusNovo()).isEqualTo("EM_DIAGNOSTICO");
        assertThat(event.getTimestamp()).isEqualTo(timestamp);
        assertThat(event.getEventType()).isEqualTo("STATUS_MUDADO");
    }

    @Test
    @DisplayName("Deve usar setters e getters corretamente")
    void deveUsarSettersEGetters() {
        StatusMudadoEvent event = new StatusMudadoEvent();
        UUID osId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();

        event.setOsId(osId);
        event.setStatusAnterior("RECEBIDA");
        event.setStatusNovo("EM_DIAGNOSTICO");
        event.setTimestamp(timestamp);

        assertThat(event.getOsId()).isEqualTo(osId);
        assertThat(event.getStatusAnterior()).isEqualTo("RECEBIDA");
        assertThat(event.getStatusNovo()).isEqualTo("EM_DIAGNOSTICO");
        assertThat(event.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    @DisplayName("Deve ter toString não nulo")
    void deveImplementarToString() {
        StatusMudadoEvent event = new StatusMudadoEvent();
        event.setOsId(UUID.randomUUID());

        assertThat(event.toString()).isNotNull();
        assertThat(event.toString()).contains("StatusMudadoEvent");
    }
}
