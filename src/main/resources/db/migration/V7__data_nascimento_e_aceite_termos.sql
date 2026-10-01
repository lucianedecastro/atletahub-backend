-- Data de nascimento da CONTA (usada para barrar menores de 18 anos) e registro do aceite dos termos.
-- Colunas opcionais: contas criadas antes desta versão ficam com data_nascimento nula
-- e o app pede a data no próximo acesso.
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS data_nascimento DATE;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS termos_aceitos_em TIMESTAMP WITH TIME ZONE;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS termos_versao VARCHAR(20);
