package br.com.grupo99.osservice.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de métricas Kafka para observabilidade.
 * Integra com Prometheus/Micrometer para dashboards New Relic/Grafana.
 * 
 * Métricas expostas:
 * - kafka.publisher.events.total: Total de eventos publicados por tipo
 * - kafka.publisher.events.failed: Total de falhas por tipo de evento
 * - kafka.publisher.latency: Latência de publicação
 * - kafka.circuitbreaker.state: Estado do Circuit Breaker
 */
@Slf4j
@Configuration
public class KafkaMetricsConfig {

    private final MeterRegistry meterRegistry;

    public KafkaMetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        log.info("📊 Kafka Metrics configurado para monitoramento");
    }

    // ===================== CONTADORES DE EVENTOS =====================

    @Bean
    public Counter osEventosPublicados() {
        return Counter.builder("kafka.publisher.events.total")
                .description("Total de eventos OS publicados com sucesso")
                .tag("service", "os-service")
                .tag("topic", KafkaConfig.TOPIC_OS_EVENTS)
                .tag("status", "success")
                .register(meterRegistry);
    }

    @Bean
    public Counter osEventosFalhos() {
        return Counter.builder("kafka.publisher.events.failed")
                .description("Total de eventos OS que falharam na publicação")
                .tag("service", "os-service")
                .tag("topic", KafkaConfig.TOPIC_OS_EVENTS)
                .tag("status", "failed")
                .register(meterRegistry);
    }

    @Bean
    public Counter dltEventosEnviados() {
        return Counter.builder("kafka.dlt.events.total")
                .description("Total de eventos enviados para Dead Letter Topics")
                .tag("service", "os-service")
                .tag("topic", "dlt")
                .register(meterRegistry);
    }

    // ===================== CONTADORES POR TIPO DE EVENTO =====================

    @Bean
    public Counter osCriadaCounter() {
        return Counter.builder("kafka.publisher.events.by_type")
                .description("Eventos por tipo")
                .tag("service", "os-service")
                .tag("event_type", "OS_CRIADA")
                .register(meterRegistry);
    }

    @Bean
    public Counter statusMudadoCounter() {
        return Counter.builder("kafka.publisher.events.by_type")
                .description("Eventos por tipo")
                .tag("service", "os-service")
                .tag("event_type", "STATUS_MUDADO")
                .register(meterRegistry);
    }

    @Bean
    public Counter osCanceladaCounter() {
        return Counter.builder("kafka.publisher.events.by_type")
                .description("Eventos por tipo - Compensação")
                .tag("service", "os-service")
                .tag("event_type", "OS_CANCELADA")
                .tag("saga_action", "compensation")
                .register(meterRegistry);
    }

    // ===================== TIMERS DE LATÊNCIA =====================

    @Bean
    public Timer kafkaPublishLatency() {
        return Timer.builder("kafka.publisher.latency")
                .description("Latência de publicação de eventos Kafka")
                .tag("service", "os-service")
                .tag("topic", KafkaConfig.TOPIC_OS_EVENTS)
                .publishPercentileHistogram(true)
                .register(meterRegistry);
    }

    @Bean
    public Timer sagaLatency() {
        return Timer.builder("saga.step.latency")
                .description("Latência de cada etapa da Saga")
                .tag("service", "os-service")
                .publishPercentileHistogram(true)
                .register(meterRegistry);
    }

    // ===================== MÉTRICAS DE CIRCUIT BREAKER =====================

    @Bean
    public Counter circuitBreakerOpenCounter() {
        return Counter.builder("kafka.circuitbreaker.opened")
                .description("Quantidade de vezes que o Circuit Breaker abriu")
                .tag("service", "os-service")
                .tag("circuit_breaker", "kafkaPublisher")
                .register(meterRegistry);
    }

    @Bean
    public Counter circuitBreakerFallbackCounter() {
        return Counter.builder("kafka.circuitbreaker.fallback")
                .description("Quantidade de chamadas ao fallback")
                .tag("service", "os-service")
                .tag("circuit_breaker", "kafkaPublisher")
                .register(meterRegistry);
    }

    // ===================== MÉTRICAS DE CONSUMER =====================

    @Bean
    public Counter eventosConsumidos() {
        return Counter.builder("kafka.consumer.events.total")
                .description("Total de eventos consumidos")
                .tag("service", "os-service")
                .tag("status", "success")
                .register(meterRegistry);
    }

    @Bean
    public Counter eventosConsumidosErro() {
        return Counter.builder("kafka.consumer.events.failed")
                .description("Total de eventos consumidos com erro")
                .tag("service", "os-service")
                .tag("status", "failed")
                .register(meterRegistry);
    }

    @Bean
    public Timer kafkaConsumeLatency() {
        return Timer.builder("kafka.consumer.latency")
                .description("Latência de processamento de eventos consumidos")
                .tag("service", "os-service")
                .publishPercentileHistogram(true)
                .register(meterRegistry);
    }
}
