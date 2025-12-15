-- ================================
-- V2 - Tabela de tradução de mensagens
-- ================================

CREATE TABLE mensagem_traducao (
    id_traducao BIGSERIAL PRIMARY KEY,

    id_mensagem BIGINT NOT NULL,

    idioma_origem VARCHAR(10) NOT NULL,
    idioma_destino VARCHAR(10) NOT NULL,

    texto_traduzido TEXT NOT NULL,

    data_traducao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_mensagem_traducao_mensagem
        FOREIGN KEY (id_mensagem)
        REFERENCES mensagem (id_mensagem)
        ON DELETE CASCADE
);

-- ================================
-- Índices
-- ================================

-- Busca rápida por mensagem
CREATE INDEX idx_mensagem_traducao_id_mensagem
    ON mensagem_traducao (id_mensagem);

-- Garante performance ao buscar tradução por idioma destino
CREATE INDEX idx_mensagem_traducao_id_mensagem_idioma_destino
    ON mensagem_traducao (id_mensagem, idioma_destino);

-- (Opcional, mas recomendado)
-- Evita traduções duplicadas para o mesmo idioma
CREATE UNIQUE INDEX uk_mensagem_traducao_mensagem_idioma
    ON mensagem_traducao (id_mensagem, idioma_destino);

