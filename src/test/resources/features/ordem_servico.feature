# language: pt
Funcionalidade: Gerenciamento de Ordem de Serviço
  Como um atendente da oficina
  Eu quero gerenciar ordens de serviço
  Para controlar o fluxo de trabalho da oficina

  Cenário: Criar nova ordem de serviço
    Quando eu criar uma ordem de serviço com os dados:
      | clienteNome     | João Silva              |
      | clienteEmail    | joao@email.com          |
      | clienteTelefone | (11) 99999-9999         |
      | veiculoPlaca    | ABC-1234                |
      | descricao       | Problema no motor       |
    Então a ordem de serviço deve ser criada com sucesso
    E o status deve ser "RECEBIDA"

  Cenário: Cancelar ordem de serviço
    Dado que existe uma ordem de serviço criada
    Quando a ordem é cancelada
    Então o status deve ser "CANCELADA"
