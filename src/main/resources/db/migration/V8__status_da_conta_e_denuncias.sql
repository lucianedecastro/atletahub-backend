-- Status da conta (suspensão feita pelo admin) e denúncias de usuários.
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ATIVA';
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS motivo_suspensao VARCHAR(500);
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS status_alterado_em TIMESTAMP WITH TIME ZONE;

CREATE TABLE IF NOT EXISTS denuncia (
    id_denuncia BIGSERIAL PRIMARY KEY,
    id_denunciante BIGINT NOT NULL REFERENCES usuario (id_usuario),
    id_denunciado BIGINT NOT NULL REFERENCES usuario (id_usuario),
    tipo_alvo VARCHAR(20) NOT NULL,
    motivo VARCHAR(30) NOT NULL,
    descricao VARCHAR(1000),
    referencia VARCHAR(500),
    -- Cópia do conteúdo denunciado (texto da mensagem ou endereço da mídia) no momento da denúncia.
    trecho TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ABERTA',
    criada_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    resolvida_em TIMESTAMP WITH TIME ZONE,
    resolvida_por BIGINT REFERENCES usuario (id_usuario),
    nota_admin VARCHAR(1000)
);

CREATE INDEX IF NOT EXISTS idx_denuncia_status_criada ON denuncia (status, criada_em DESC);
CREATE INDEX IF NOT EXISTS idx_denuncia_denunciado ON denuncia (id_denunciado);
CREATE INDEX IF NOT EXISTS idx_denuncia_denunciante_criada ON denuncia (id_denunciante, criada_em);
