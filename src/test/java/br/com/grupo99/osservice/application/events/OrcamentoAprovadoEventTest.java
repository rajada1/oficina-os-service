package br.com.grupo99.osservice.application.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrcamentoAprovadoEvent - Testes Unitários")
class OrcamentoAprovadoEventTest {

    @Test
    @DisplayName("Deve criar evento com construtor padrão")
    void deveCriarEventoComConstrutorPadrao() {
        OrcamentoAprovadoEvent event = new OrcamentoAprovadoEvent();

        assertThat(event.getOrcamentoId()).isNull();
        assertThat(event.getOsId()).isNull();
        assertThat(event.getTimestamp()).isNull();
        assertThat(event.getEventType()).isEqualTo("ORCAMENTO_APROVADO");
    }

    @Test
    @DisplayName("Deve criar evento com todos os argumentos")
    void deveCriarEventoComTodosOsArgumentos() {
        UUID orcamentoId = UUID.randomUUID();
        UUID osId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();

        OrcamentoAprovadoEvent event = new OrcamentoAprovadoEvent(orcamentoId, osId, timestamp, "ORCAMENTO_APROVADO");

        assertThat(event.getOrcamentoId()).isEqualTo(orcamentoId);
        assertThat(event.getOsId()).isEqualTo(osId);
        assertThat(event.getTimestamp()).isEqualTo(timestamp);
        assertThat(event.getEventType()).isEqualTo("ORCAMENTO_APROVADO");
    }

    @Test
    @DisplayName("Deve usar setters e getters corretamente")
    void deveUsarSettersEGetters() {
        OrcamentoAprovadoEvent event = new OrcamentoAprovadoEvent();
        UUID orcamentoId = UUID.randomUUID();
        UUID osId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();

        event.setOrcamentoId(orcamentoId);
        event.setOsId(osId);
        event.setTimestamp(timestamp);
        event.setEventType("CUSTOM");

        assertThat(event.getOrcamentoId()).isEqualTo(orcamentoId);
        assertThat(event.getOsId()).isEqualTo(osId);
        assertThat(event.getTimestamp()).isEqualTo(timestamp);
        assertThat(event.getEventType()).isEqualTo("CUSTOM");
    }

    @Test
    @DisplayName("Deve ter toString não nulo")
    void deveImplementarToString() {
        OrcamentoAprovadoEvent event = new OrcamentoAprovadoEvent();
        event.setOsId(UUID.randomUUID());

        assertThat(event.toString()).isNotNull();
        assertThat(event.toString()).contains("OrcamentoAprovadoEvent");
    }
}
