package br.com.grupo99.osservice.infrastructure.messaging;

import br.com.grupo99.osservice.application.events.OSCanceladaEvent;
import br.com.grupo99.osservice.application.events.OSCriadaEvent;
import br.com.grupo99.osservice.application.events.StatusMudadoEvent;

/**
 * Interface de abstração para publicação de eventos.
 * Permite trocar facilmente entre SQS e Kafka.
 */
public interface EventPublisherPort {

    /**
     * Publica evento de OS criada (Saga Step 1)
     */
    void publishOSCriada(OSCriadaEvent event);

    /**
     * Publica evento de mudança de status (Saga Step intermediário)
     */
    void publishStatusMudado(StatusMudadoEvent event);

    /**
     * Publica evento de compensação - OS cancelada (Rollback)
     */
    void publishOSCancelada(OSCanceladaEvent event);
}
