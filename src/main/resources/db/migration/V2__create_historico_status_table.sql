-- Criar tabela historico_status
CREATE TABLE IF NOT EXISTS historico_status (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ordem_servico_id UUID NOT NULL,
    status_anterior VARCHAR(30),
    novo_status VARCHAR(30) NOT NULL,
    observacao TEXT,
    usuario_alteracao VARCHAR(100),
    data_alteracao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_historico_ordem_servico 
        FOREIGN KEY (ordem_servico_id) 
        REFERENCES ordem_servico(id) 
        ON DELETE CASCADE
);

-- Criar índices
CREATE INDEX idx_historico_os ON historico_status(ordem_servico_id);
CREATE INDEX idx_historico_data ON historico_status(data_alteracao DESC);

-- Comentários
COMMENT ON TABLE historico_status IS 'Histórico de todas as mudanças de status das ordens de serviço';
COMMENT ON COLUMN historico_status.status_anterior IS 'Status antes da mudança (null na criação)';
COMMENT ON COLUMN historico_status.novo_status IS 'Novo status após a mudança';
