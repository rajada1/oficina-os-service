# Migração SQS para Apache Kafka - OS Service

## Visão Geral

Este documento descreve a migração da infraestrutura de mensageria do **AWS SQS** para **Apache Kafka** no serviço de Ordens de Serviço (OS), como parte da implementação de uma arquitetura de eventos (Event-Driven Architecture).

## Arquitetura de Eventos

### Padrão: Saga Coreografada

A arquitetura implementa o padrão **Saga Coreografada** para gerenciar transações distribuídas entre microserviços:

```
┌─────────────┐    OS_CRIADA     ┌─────────────┐
│  OS-Service │─────────────────►│  Billing    │
│             │                  │  Service    │
│             │◄─────────────────│             │
│             │ ORCAMENTO_APROVADO              │
└─────────────┘                  └─────────────┘
       │                                │
       │ STATUS_MUDADO                  │
       ▼                                ▼
┌─────────────┐  EXECUCAO_CONCLUIDA    ┌─────────────┐
│ Notification│◄──────────────────────│  Execution  │
│  Service    │                        │  Service    │
└─────────────┘                        └─────────────┘
```

### Tópicos Kafka

| Tópico | Partições | Retenção | Descrição |
|--------|-----------|----------|-----------|
| `os-events` | 6 | 30 dias | Eventos de criação e atualização de OS |
| `billing-events` | 3 | 30 dias | Eventos de orçamento e faturamento |
| `execution-events` | 3 | 30 dias | Eventos de execução de serviços |

### Eventos

#### Publicados pelo OS-Service (Producer)
- `OS_CRIADA` - Nova OS criada
- `STATUS_MUDADO` - Mudança de status da OS
- `OS_CANCELADA` - OS cancelada (evento de compensação)

#### Consumidos pelo OS-Service (Consumer)
- `ORCAMENTO_APROVADO` - Orçamento aprovado pelo cliente
- `ORCAMENTO_REJEITADO` - Orçamento rejeitado
- `EXECUCAO_CONCLUIDA` - Serviço executado com sucesso
- `EXECUCAO_FALHOU` - Falha na execução

## Benefícios da Migração

### Kafka vs SQS

| Característica | SQS | Kafka |
|---------------|-----|-------|
| **Ordenação** | Por grupo de mensagens | Por partição (garantida) |
| **Replay** | Não suportado | Suportado (offset reset) |
| **Throughput** | ~3K msg/s | ~100K+ msg/s |
| **Retenção** | 14 dias máx | Configurável (30 dias) |
| **Consumer Groups** | Limitado | Nativo |
| **Event Sourcing** | Não adequado | Ideal |
| **Custo** | Por requisição | Por cluster |

## Estrutura de Código

### Novos Arquivos

```
src/main/java/br/com/grupo99/osservice/
└── infrastructure/
    ├── config/
    │   └── KafkaConfig.java          # Configuração Kafka
    └── messaging/
        ├── EventPublisherPort.java   # Interface de abstração
        ├── KafkaEventPublisher.java  # Publisher Kafka
        └── KafkaEventListener.java   # Consumer Kafka
```

### Configuração

#### application.yml
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
    consumer:
      group-id: ${KAFKA_CONSUMER_GROUP:os-service-group}
      auto-offset-reset: earliest
      enable-auto-commit: false
    listener:
      ack-mode: manual
```

### Docker Compose

O ambiente local inclui:
- **Zookeeper** - Coordenação do cluster
- **Kafka** - Broker de mensagens
- **Kafka UI** - Interface de monitoramento (http://localhost:8080)
- **kafka-init** - Criação automática dos tópicos

## Execução Local

```bash
# Subir infraestrutura
docker-compose up -d

# Verificar tópicos
docker exec -it os-service-kafka kafka-topics --list --bootstrap-server localhost:9092

# Monitorar mensagens
docker exec -it os-service-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic os-events \
  --from-beginning
```

## Testes

```bash
# Testes unitários e integração com Embedded Kafka
mvn test

# Testes específicos de Kafka
mvn test -Dtest=KafkaEventPublisherTest,KafkaEventListenerTest
```

## Variáveis de Ambiente

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Endereço do broker |
| `KAFKA_CONSUMER_GROUP` | `os-service-group` | ID do consumer group |

## Migração Gradual

A migração foi implementada com suporte a ambos sistemas:

1. **Interface `EventPublisherPort`** - Abstração comum
2. **`@Primary` no KafkaEventPublisher** - Kafka é o padrão
3. **SQS mantido** - Pode ser reativado via profile

Para usar SQS novamente:
```yaml
spring:
  profiles:
    active: sqs
```

## Próximos Passos

1. [ ] Migrar `billing-service` para Kafka
2. [ ] Migrar `execution-service` para Kafka
3. [ ] Configurar Dead Letter Topics (DLT)
4. [ ] Implementar Kafka Streams para processamento complexo
5. [ ] Deploy do Kafka/MSK na AWS

## Referências

- [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Event-Driven Architecture Patterns](https://microservices.io/patterns/data/saga.html)
