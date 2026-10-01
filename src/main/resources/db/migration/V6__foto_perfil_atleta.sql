-- Foto de perfil do atleta (a marca já usa perfil_marca.logo_url, criada na V4).
ALTER TABLE perfil_atleta ADD COLUMN IF NOT EXISTS foto_url VARCHAR(500);
