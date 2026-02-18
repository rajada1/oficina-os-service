package br.com.grupo99.osservice.application.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OSCanceladaEvent - Testes Unitários")
class OSCanceladaEventTest {

    @Test
    @DisplayName("Deve criar evento com construtor padrão")
    void deveCriarEventoComConstrutorPadrao() {
        OSCanceladaEvent event = new OSCanceladaEvent();

        assertThat(event.getOsId()).isNull();
        assertThat(event.getMotivo()).isNull();
        assertThat(event.getEtapaFalha()).isNull();
        assertThat(event.getTimestamp()).isNull();
        assertThat(event.getEventType()).isEqualTo("OS_CANCELADA");
    }

    @Test
    @DisplayName("Deve criar evento com todos os argumentos")
    void deveCriarEventoComTodosOsArgumentos() {
        UUID osId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();

        OSCanceladaEvent event = new OSCanceladaEvent(osId, "Falha no pagamento", "PAYMENT", timestamp, "OS_CANCELADA");

        assertThat(event.getOsId()).isEqualTo(osId);
        assertThat(event.getMotivo()).isEqualTo("Falha no pagamento");
        assertThat(event.getEtapaFalha()).isEqualTo("PAYMENT");
        assertThat(event.getTimestamp()).isEqualTo(timestamp);
        assertThat(event.getEventType()).isEqualTo("OS_CANCELADA");
    }

    @Test
    @DisplayName("Deve usar setters e getters corretamente")
    void deveUsarSettersEGetters() {
        OSCanceladaEvent event = new OSCanceladaEvent();
        UUID osId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();

        event.setOsId(osId);
        event.setMotivo("Motivo teste");
        event.setEtapaFalha("BILLING");
        event.setTimestamp(timestamp);
        event.setEventType("CUSTOM");

        assertThat(event.getOsId()).isEqualTo(osId);
        assertThat(event.getMotivo()).isEqualTo("Motivo teste");
        assertThat(event.getEtapaFalha()).isEqualTo("BILLING");
        assertThat(event.getTimestamp()).isEqualTo(timestamp);
        assertThat(event.getEventType()).isEqualTo("CUSTOM");
    }

    @Test
    @DisplayName("Deve ter toString não nulo")
    void deveImplementarToString() {
        OSCanceladaEvent event = new OSCanceladaEvent();
        event.setOsId(UUID.randomUUID());

        assertThat(event.toString()).isNotNull();
        assertThat(event.toString()).contains("OSCanceladaEvent");
    }
}
