package br.com.grupo99.osservice.application.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OSCriadaEvent - Testes Unitários")
class OSCriadaEventTest {

    @Test
    @DisplayName("Deve criar evento com construtor padrão")
    void deveCriarEventoComConstrutorPadrao() {
        OSCriadaEvent event = new OSCriadaEvent();

        assertThat(event.getOsId()).isNull();
        assertThat(event.getClienteId()).isNull();
        assertThat(event.getVeiculoId()).isNull();
        assertThat(event.getDescricao()).isNull();
        assertThat(event.getTimestamp()).isNull();
        assertThat(event.getEventType()).isEqualTo("OS_CRIADA");
    }

    @Test
    @DisplayName("Deve criar evento com todos os argumentos")
    void deveCriarEventoComTodosOsArgumentos() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();

        OSCriadaEvent event = new OSCriadaEvent(osId, clienteId, veiculoId, "Descrição", timestamp, "OS_CRIADA");

        assertThat(event.getOsId()).isEqualTo(osId);
        assertThat(event.getClienteId()).isEqualTo(clienteId);
        assertThat(event.getVeiculoId()).isEqualTo(veiculoId);
        assertThat(event.getDescricao()).isEqualTo("Descrição");
        assertThat(event.getTimestamp()).isEqualTo(timestamp);
        assertThat(event.getEventType()).isEqualTo("OS_CRIADA");
    }

    @Test
    @DisplayName("Deve usar setters e getters corretamente")
    void deveUsarSettersEGetters() {
        OSCriadaEvent event = new OSCriadaEvent();
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();

        event.setOsId(osId);
        event.setClienteId(clienteId);
        event.setVeiculoId(veiculoId);
        event.setDescricao("Teste");
        event.setTimestamp(timestamp);
        event.setEventType("CUSTOM_EVENT");

        assertThat(event.getOsId()).isEqualTo(osId);
        assertThat(event.getClienteId()).isEqualTo(clienteId);
        assertThat(event.getVeiculoId()).isEqualTo(veiculoId);
        assertThat(event.getDescricao()).isEqualTo("Teste");
        assertThat(event.getTimestamp()).isEqualTo(timestamp);
        assertThat(event.getEventType()).isEqualTo("CUSTOM_EVENT");
    }

    @Test
    @DisplayName("Deve implementar equals e hashCode")
    void deveImplementarEqualsEHashCode() {
        UUID osId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        OSCriadaEvent event1 = new OSCriadaEvent(osId, UUID.randomUUID(), UUID.randomUUID(), "Desc", now, "OS_CRIADA");
        OSCriadaEvent event2 = new OSCriadaEvent(osId, event1.getClienteId(), event1.getVeiculoId(), "Desc", now, "OS_CRIADA");

        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
    }

    @Test
    @DisplayName("Deve ter toString não nulo")
    void deveImplementarToString() {
        OSCriadaEvent event = new OSCriadaEvent();
        event.setOsId(UUID.randomUUID());

        assertThat(event.toString()).isNotNull();
        assertThat(event.toString()).contains("OSCriadaEvent");
    }
}
