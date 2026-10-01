-- V5: correções de dados e índices
--
-- 1) ALTURA: a coluna era DECIMAL(3,2) (máx. 9.99), mas o app trabalha em CENTÍMETROS (ex.: 175).
--    Amplia para NUMERIC(5,2) e converte valores antigos que estavam em METROS (ex.: 1.75 -> 175.00).
ALTER TABLE perfil_atleta ALTER COLUMN altura TYPE NUMERIC(5,2);
UPDATE perfil_atleta SET altura = altura * 100 WHERE altura IS NOT NULL AND altura < 3;

-- 2) INTERESSE: garante no banco que cada usuário só tem UM interesse por destino.
--    Antes de criar o índice único, remove eventuais duplicados (mantém o registro mais antigo).
DELETE FROM interesse a
USING interesse b
WHERE a.id_origem = b.id_origem
  AND a.id_destino = b.id_destino
  AND a.id_interesse > b.id_interesse;

CREATE UNIQUE INDEX IF NOT EXISTS uk_interesse_origem_destino ON interesse (id_origem, id_destino);

-- 3) Índices para as consultas mais frequentes (listar conversa, interesses recebidos, matches).
CREATE INDEX IF NOT EXISTS idx_interesse_destino ON interesse (id_destino);
CREATE INDEX IF NOT EXISTS idx_mensagem_match_data ON mensagem (id_match, data_envio);
CREATE INDEX IF NOT EXISTS idx_match_usuario_b ON match_table (id_usuario_b);
