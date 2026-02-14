-- Criar tabela ordem_servico
CREATE TABLE IF NOT EXISTS ordem_servico (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id UUID NOT NULL,
    veiculo_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RECEBIDA',
    descricao_problema TEXT,
    valor_total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_finalizacao TIMESTAMP,
    data_entrega TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Criar índices
CREATE INDEX idx_os_status ON ordem_servico(status);
CREATE INDEX idx_os_cliente ON ordem_servico(cliente_id);
CREATE INDEX idx_os_veiculo ON ordem_servico(veiculo_id);
CREATE INDEX idx_os_data_criacao ON ordem_servico(data_criacao DESC);

-- Comentários
COMMENT ON TABLE ordem_servico IS 'Tabela principal de ordens de serviço';
COMMENT ON COLUMN ordem_servico.status IS 'Status atual da OS: RECEBIDA, EM_DIAGNOSTICO, AGUARDANDO_APROVACAO, AGUARDANDO_PAGAMENTO, EM_EXECUCAO, FINALIZADA, ENTREGUE, CANCELADA';
COMMENT ON COLUMN ordem_servico.version IS 'Versão para optimistic locking';
