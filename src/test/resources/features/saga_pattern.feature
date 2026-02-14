# language: pt
Funcionalidade: Saga Pattern - Fluxo de Ordem de Serviço
  Como sistema de gestão de oficina
  Eu quero coordenar transações distribuídas entre microserviços
  Para garantir consistência eventual e compensação em caso de falhas

  Contexto:
    Dado que os microserviços estão disponíveis
    E as filas SQS estão configuradas

  Cenário: Criar nova Ordem de Serviço
    Dado que um cliente solicita um serviço
    Quando eu criar uma OS com os dados:
      | clienteNome     | Maria Santos            |
      | clienteEmail    | maria@email.com         |
      | clienteTelefone | (11) 98888-8888         |
      | veiculoPlaca    | XYZ-5678                |
      | descricao       | Troca de óleo e filtros |
    Então o evento "OS_CRIADA" deve ser publicado na fila "os-events-queue"
    E o Billing Service deve receber o evento
    E o Execution Service deve receber o evento
    E a OS deve estar no status "RECEBIDA"

  Cenário: Cancelamento Manual da OS pelo Atendente
    Dado que existe uma OS no status "RECEBIDA"
    Quando o atendente cancela manualmente a OS
    Então o evento "OS_CANCELADA" deve ser publicado
    E a OS deve mudar para status "CANCELADA"
