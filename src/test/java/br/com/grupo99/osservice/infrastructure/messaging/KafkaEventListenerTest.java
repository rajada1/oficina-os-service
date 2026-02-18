package br.com.grupo99.osservice.infrastructure.messaging;

import br.com.grupo99.osservice.domain.model.OrdemServico;
import br.com.grupo99.osservice.domain.model.StatusOS;
import br.com.grupo99.osservice.domain.repository.OrdemServicoRepository;
import br.com.grupo99.osservice.infrastructure.config.KafkaConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes de integração para o KafkaEventListener usando Embedded Kafka
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = {
        KafkaConfig.TOPIC_OS_EVENTS,
        KafkaConfig.TOPIC_BILLING_EVENTS,
        KafkaConfig.TOPIC_EXECUTION_EVENTS
}, brokerProperties = {
        "listeners=PLAINTEXT://localhost:9094",
        "port=9094"
})
@org.springframework.test.context.TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}" })
class KafkaEventListenerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @MockBean
    private OrdemServicoRepository ordemServicoRepository;

    private KafkaTemplate<String, Object> kafkaTemplate;

    private OrdemServico mockOrdemServico;

    @BeforeEach
    void setUp() {
        // Configurar producer para enviar mensagens de teste
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        kafkaTemplate = new KafkaTemplate<>(producerFactory);

        // Setup mock OrdemServico
        mockOrdemServico = mock(OrdemServico.class);
        when(ordemServicoRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockOrdemServico));
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenReturn(mockOrdemServico);
    }

    @Test
    @DisplayName("Deve processar evento ORCAMENTO_APROVADO e atualizar status para EM_EXECUCAO")
    void deveProcessarOrcamentoAprovado() {
        // Arrange
        UUID osId = UUID.randomUUID();
        Map<String, Object> payload = Map.of(
                "osId", osId.toString(),
                "valorAprovado", 1500.00,
                "aprovadoPor", "cliente@email.com");

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaConfig.TOPIC_BILLING_EVENTS,
                osId.toString(),
                payload);
        record.headers().add(new RecordHeader("eventType", "ORCAMENTO_APROVADO".getBytes(StandardCharsets.UTF_8)));

        // Act
        kafkaTemplate.send(record);

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(mockOrdemServico, times(1)).atualizarStatus(eq(StatusOS.EM_EXECUCAO), anyString(), anyString());
            verify(ordemServicoRepository, times(1)).save(any(OrdemServico.class));
        });
    }

    @Test
    @DisplayName("Deve processar evento ORCAMENTO_REJEITADO e atualizar status")
    void deveProcessarOrcamentoRejeitado() {
        // Arrange
        UUID osId = UUID.randomUUID();
        Map<String, Object> payload = Map.of(
                "osId", osId.toString(),
                "motivo", "Valor muito alto");

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaConfig.TOPIC_BILLING_EVENTS,
                osId.toString(),
                payload);
        record.headers().add(new RecordHeader("eventType", "ORCAMENTO_REJEITADO".getBytes(StandardCharsets.UTF_8)));

        // Act
        kafkaTemplate.send(record);

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(mockOrdemServico, times(1)).atualizarStatus(eq(StatusOS.CANCELADA), anyString(), anyString());
            verify(ordemServicoRepository, times(1)).save(any(OrdemServico.class));
        });
    }

    @Test
    @DisplayName("Deve processar evento EXECUCAO_CONCLUIDA e finalizar OS")
    void deveProcessarExecucaoConcluida() {
        // Arrange
        UUID osId = UUID.randomUUID();
        Map<String, Object> payload = Map.of(
                "osId", osId.toString(),
                "observacoes", "Serviço realizado com sucesso",
                "executadoPor", "mecanico01");

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaConfig.TOPIC_EXECUTION_EVENTS,
                osId.toString(),
                payload);
        record.headers().add(new RecordHeader("eventType", "EXECUCAO_CONCLUIDA".getBytes(StandardCharsets.UTF_8)));

        // Act
        kafkaTemplate.send(record);

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(mockOrdemServico, times(1)).atualizarStatus(eq(StatusOS.FINALIZADA), anyString(), anyString());
            verify(ordemServicoRepository, times(1)).save(any(OrdemServico.class));
        });
    }

    @Test
    @DisplayName("Deve processar evento EXECUCAO_FALHOU com retrabalho")
    void deveProcessarExecucaoFalhouComRetrabalho() {
        // Arrange
        UUID osId = UUID.randomUUID();
        Map<String, Object> payload = Map.of(
                "osId", osId.toString(),
                "motivo", "Peça defeituosa",
                "requerRetrabalho", true);

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaConfig.TOPIC_EXECUTION_EVENTS,
                osId.toString(),
                payload);
        record.headers().add(new RecordHeader("eventType", "EXECUCAO_FALHOU".getBytes(StandardCharsets.UTF_8)));

        // Act
        kafkaTemplate.send(record);

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(mockOrdemServico, times(1)).atualizarStatus(eq(StatusOS.EM_EXECUCAO), anyString(), anyString());
            verify(ordemServicoRepository, times(1)).save(any(OrdemServico.class));
        });
    }

    @Test
    @DisplayName("Deve processar evento EXECUCAO_FALHOU sem retrabalho")
    void deveProcessarExecucaoFalhouSemRetrabalho() {
        // Arrange
        UUID osId = UUID.randomUUID();
        Map<String, Object> payload = Map.of(
                "osId", osId.toString(),
                "motivo", "Erro irreversível",
                "requerRetrabalho", false);

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaConfig.TOPIC_EXECUTION_EVENTS,
                osId.toString(),
                payload);
        record.headers().add(new RecordHeader("eventType", "EXECUCAO_FALHOU".getBytes(StandardCharsets.UTF_8)));

        // Act
        kafkaTemplate.send(record);

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(mockOrdemServico, times(1)).atualizarStatus(eq(StatusOS.CANCELADA), anyString(), anyString());
            verify(ordemServicoRepository, times(1)).save(any(OrdemServico.class));
        });
    }

    @Test
    @DisplayName("Deve ignorar eventos com tipo desconhecido")
    void deveIgnorarEventoTipoDesconhecido() {
        // Arrange
        UUID osId = UUID.randomUUID();
        Map<String, Object> payload = Map.of("osId", osId.toString());

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaConfig.TOPIC_BILLING_EVENTS,
                osId.toString(),
                payload);
        record.headers().add(new RecordHeader("eventType", "EVENTO_DESCONHECIDO".getBytes(StandardCharsets.UTF_8)));

        // Act
        kafkaTemplate.send(record);

        // Assert - deve aguardar mas não chamar o repository para atualizar
        await().during(3, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(mockOrdemServico, never()).atualizarStatus(any(StatusOS.class), anyString(), anyString());
        });
    }
}
