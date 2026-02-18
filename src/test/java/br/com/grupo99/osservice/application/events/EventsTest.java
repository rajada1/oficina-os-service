package br.com.grupo99.osservice.application.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Events da aplicação")
class EventsTest {

    @Test
    @DisplayName("OSCriadaEvent - deve criar com todos os campos via construtor")
    void osCriadaEvent_deveCriarComTodosCampos() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        OSCriadaEvent event = new OSCriadaEvent(osId, clienteId, veiculoId, "Troca de óleo", now, "OS_CRIADA");

        assertThat(event.getOsId()).isEqualTo(osId);
        assertThat(event.getClienteId()).isEqualTo(clienteId);
        assertThat(event.getVeiculoId()).isEqualTo(veiculoId);
        assertThat(event.getDescricao()).isEqualTo("Troca de óleo");
        assertThat(event.getTimestamp()).isEqualTo(now);
        assertThat(event.getEventType()).isEqualTo("OS_CRIADA");
    }

    @Test
    @DisplayName("OSCriadaEvent - deve criar vazio e usar setters")
    void osCriadaEvent_deveCriarVazio() {
        OSCriadaEvent event = new OSCriadaEvent();
        UUID osId = UUID.randomUUID();
        event.setOsId(osId);
        event.setDescricao("Revisão");

        assertThat(event.getOsId()).isEqualTo(osId);
        assertThat(event.getDescricao()).isEqualTo("Revisão");
    }

    @Test
    @DisplayName("OSCriadaEvent - equals, hashCode e toString")
    void osCriadaEvent_equalsHashCodeToString() {
        UUID osId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        OSCriadaEvent event1 = new OSCriadaEvent(osId, clienteId, veiculoId, "desc", now, "OS_CRIADA");
        OSCriadaEvent event2 = new OSCriadaEvent(osId, clienteId, veiculoId, "desc", now, "OS_CRIADA");

        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        assertThat(event1.toString()).contains(osId.toString());
    }

    @Test
    @DisplayName("OSCanceladaEvent - deve criar com todos os campos")
    void osCanceladaEvent_deveCriarComTodosCampos() {
        UUID osId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        OSCanceladaEvent event = new OSCanceladaEvent(osId, "Motivo", "BILLING", now, "OS_CANCELADA");

        assertThat(event.getOsId()).isEqualTo(osId);
        assertThat(event.getMotivo()).isEqualTo("Motivo");
        assertThat(event.getEtapaFalha()).isEqualTo("BILLING");
        assertThat(event.getTimestamp()).isEqualTo(now);
        assertThat(event.getEventType()).isEqualTo("OS_CANCELADA");
    }

    @Test
    @DisplayName("OSCanceladaEvent - deve criar vazio e usar setters")
    void osCanceladaEvent_deveCriarVazio() {
        OSCanceladaEvent event = new OSCanceladaEvent();
        UUID osId = UUID.randomUUID();
        event.setOsId(osId);
        event.setMotivo("Cancelamento");
        event.setEtapaFalha("EXECUTION");
        event.setTimestamp(LocalDateTime.now());
        event.setEventType("OS_CANCELADA");

        assertThat(event.getOsId()).isEqualTo(osId);
        assertThat(event.getMotivo()).isEqualTo("Cancelamento");
        assertThat(event.getEtapaFalha()).isEqualTo("EXECUTION");
    }

    @Test
    @DisplayName("OSCanceladaEvent - equals e hashCode")
    void osCanceladaEvent_equalsHashCode() {
        UUID osId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        OSCanceladaEvent event1 = new OSCanceladaEvent(osId, "Motivo", "BILLING", now, "OS_CANCELADA");
        OSCanceladaEvent event2 = new OSCanceladaEvent(osId, "Motivo", "BILLING", now, "OS_CANCELADA");

        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
    }

    @Test
    @DisplayName("StatusMudadoEvent - deve criar com todos os campos")
    void statusMudadoEvent_deveCriarComTodosCampos() {
        UUID osId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        StatusMudadoEvent event = new StatusMudadoEvent(osId, "ABERTA", "EM_DIAGNOSTICO", now, "STATUS_MUDADO");

        assertThat(event.getOsId()).isEqualTo(osId);
        assertThat(event.getStatusAnterior()).isEqualTo("ABERTA");
        assertThat(event.getStatusNovo()).isEqualTo("EM_DIAGNOSTICO");
        assertThat(event.getTimestamp()).isEqualTo(now);
        assertThat(event.getEventType()).isEqualTo("STATUS_MUDADO");
    }

    @Test
    @DisplayName("StatusMudadoEvent - deve criar vazio e usar setters")
    void statusMudadoEvent_deveCriarVazio() {
        StatusMudadoEvent event = new StatusMudadoEvent();
        UUID osId = UUID.randomUUID();
        event.setOsId(osId);
        event.setStatusAnterior("RECEBIDA");
        event.setStatusNovo("EM_DIAGNOSTICO");
        event.setTimestamp(LocalDateTime.now());
        event.setEventType("STATUS_MUDADO");

        assertThat(event.getOsId()).isEqualTo(osId);
        assertThat(event.getStatusAnterior()).isEqualTo("RECEBIDA");
        assertThat(event.getStatusNovo()).isEqualTo("EM_DIAGNOSTICO");
    }

    @Test
    @DisplayName("StatusMudadoEvent - equals e hashCode")
    void statusMudadoEvent_equalsHashCode() {
        UUID osId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        StatusMudadoEvent event1 = new StatusMudadoEvent(osId, "A", "B", now, "STATUS_MUDADO");
        StatusMudadoEvent event2 = new StatusMudadoEvent(osId, "A", "B", now, "STATUS_MUDADO");

        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        assertThat(event1.toString()).contains(osId.toString());
    }

    @Test
    @DisplayName("OrcamentoAprovadoEvent - deve criar com todos os campos")
    void orcamentoAprovadoEvent_deveCriarComTodosCampos() {
        UUID orcamentoId = UUID.randomUUID();
        UUID osId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        OrcamentoAprovadoEvent event = new OrcamentoAprovadoEvent(orcamentoId, osId, now, "ORCAMENTO_APROVADO");

        assertThat(event.getOrcamentoId()).isEqualTo(orcamentoId);
        assertThat(event.getOsId()).isEqualTo(osId);
        assertThat(event.getTimestamp()).isEqualTo(now);
        assertThat(event.getEventType()).isEqualTo("ORCAMENTO_APROVADO");
    }

    @Test
    @DisplayName("OrcamentoAprovadoEvent - deve criar vazio e usar setters")
    void orcamentoAprovadoEvent_deveCriarVazio() {
        OrcamentoAprovadoEvent event = new OrcamentoAprovadoEvent();
        UUID orcamentoId = UUID.randomUUID();
        UUID osId = UUID.randomUUID();
        event.setOrcamentoId(orcamentoId);
        event.setOsId(osId);
        event.setTimestamp(LocalDateTime.now());
        event.setEventType("ORCAMENTO_APROVADO");

        assertThat(event.getOrcamentoId()).isEqualTo(orcamentoId);
        assertThat(event.getOsId()).isEqualTo(osId);
    }

    @Test
    @DisplayName("OrcamentoAprovadoEvent - equals e hashCode")
    void orcamentoAprovadoEvent_equalsHashCode() {
        UUID orcamentoId = UUID.randomUUID();
        UUID osId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        OrcamentoAprovadoEvent event1 = new OrcamentoAprovadoEvent(orcamentoId, osId, now, "ORCAMENTO_APROVADO");
        OrcamentoAprovadoEvent event2 = new OrcamentoAprovadoEvent(orcamentoId, osId, now, "ORCAMENTO_APROVADO");

        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        assertThat(event1.toString()).contains(osId.toString());
    }
}
