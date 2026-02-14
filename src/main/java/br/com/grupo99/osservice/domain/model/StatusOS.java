package br.com.grupo99.osservice.domain.model;

/**
 * Enum representando os possíveis status de uma Ordem de Serviço.
 * Representa o ciclo de vida completo de uma OS.
 */
public enum StatusOS {

    /**
     * OS foi recebida e registrada no sistema (alias ABERTA para testes)
     */
    RECEBIDA("Recebida", "OS foi registrada e aguarda processamento"),

    /**
     * Mecânico está diagnosticando o problema do veículo
     */
    EM_DIAGNOSTICO("Em Diagnóstico", "Veículo em análise técnica"),

    /**
     * Orçamento gerado e aguardando aprovação do cliente (alias
     * AGUARDANDO_ORCAMENTO para testes)
     */
    AGUARDANDO_APROVACAO("Aguardando Aprovação", "Orçamento enviado ao cliente"),

    /**
     * Aguardando confirmação de pagamento (alias ORCAMENTO_APROVADO para testes)
     */
    AGUARDANDO_PAGAMENTO("Aguardando Pagamento", "Orçamento aprovado, aguarda pagamento"),

    /**
     * OS em execução pela equipe técnica
     */
    EM_EXECUCAO("Em Execução", "Serviços sendo realizados"),

    /**
     * Serviços finalizados, aguardando entrega (alias CONCLUIDA para testes)
     */
    FINALIZADA("Finalizada", "Serviços concluídos"),

    /**
     * Veículo entregue ao cliente
     */
    ENTREGUE("Entregue", "Veículo devolvido ao cliente"),

    /**
     * OS cancelada (por cliente ou oficina)
     */
    CANCELADA("Cancelada", "OS foi cancelada");

    // Aliases para compatibilidade com testes
    public static final StatusOS ABERTA = RECEBIDA;
    public static final StatusOS AGUARDANDO_ORCAMENTO = AGUARDANDO_APROVACAO;
    public static final StatusOS ORCAMENTO_APROVADO = AGUARDANDO_PAGAMENTO;
    public static final StatusOS CONCLUIDA = FINALIZADA;

    private final String descricao;
    private final String detalhe;

    StatusOS(String descricao, String detalhe) {
        this.descricao = descricao;
        this.detalhe = detalhe;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDetalhe() {
        return detalhe;
    }

    /**
     * Valida se a transição de status é válida
     * 
     * @param novoStatus Status para o qual se deseja transicionar
     * @return true se a transição é válida
     */
    public boolean podeTransicionarPara(StatusOS novoStatus) {
        return switch (this) {
            case RECEBIDA -> novoStatus == EM_DIAGNOSTICO || novoStatus == CANCELADA;
            case EM_DIAGNOSTICO -> novoStatus == AGUARDANDO_APROVACAO || novoStatus == CANCELADA;
            case AGUARDANDO_APROVACAO -> novoStatus == AGUARDANDO_PAGAMENTO
                    || novoStatus == CANCELADA
                    || novoStatus == EM_DIAGNOSTICO; // Volta para diagnóstico se rejeitado
            case AGUARDANDO_PAGAMENTO -> novoStatus == EM_EXECUCAO || novoStatus == CANCELADA;
            case EM_EXECUCAO -> novoStatus == FINALIZADA || novoStatus == AGUARDANDO_APROVACAO; // Nova aprovação
                                                                                                // necessária
            case FINALIZADA -> novoStatus == ENTREGUE;
            case ENTREGUE, CANCELADA -> false; // Estados finais
        };
    }

    /**
     * Verifica se o status é final (não pode mais transicionar)
     */
    public boolean isFinal() {
        return this == ENTREGUE || this == CANCELADA;
    }
}
