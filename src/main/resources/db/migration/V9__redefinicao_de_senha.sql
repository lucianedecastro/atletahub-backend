-- Links de "esqueci minha senha": guardamos só o hash do código (nunca o código em si),
-- com validade curta e uso único.
CREATE TABLE IF NOT EXISTS redefinicao_senha (
    id_redefinicao BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL REFERENCES usuario (id_usuario),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    criada_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    expira_em TIMESTAMP WITH TIME ZONE NOT NULL,
    usada_em TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_redefinicao_usuario_criada ON redefinicao_senha (id_usuario, criada_em);
CREATE INDEX IF NOT EXISTS idx_redefinicao_expira ON redefinicao_senha (expira_em);
