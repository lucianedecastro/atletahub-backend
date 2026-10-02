-- Bloqueio entre usuários: quem bloqueia (id_bloqueador) deixa de ver e de ser visto por quem foi
-- bloqueado (id_bloqueado) no Descobrir, e a conversa que já existia fica congelada (sem mensagens novas).
CREATE TABLE IF NOT EXISTS bloqueio (
    id_bloqueio BIGSERIAL PRIMARY KEY,
    id_bloqueador BIGINT NOT NULL REFERENCES usuario (id_usuario),
    id_bloqueado BIGINT NOT NULL REFERENCES usuario (id_usuario),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_bloqueio_par UNIQUE (id_bloqueador, id_bloqueado),
    CONSTRAINT ck_bloqueio_pessoas_diferentes CHECK (id_bloqueador <> id_bloqueado)
);

CREATE INDEX IF NOT EXISTS idx_bloqueio_bloqueado ON bloqueio (id_bloqueado);
