-- V1__init_postgres.sql (Versão Final Corrigida)

-- Tabela de Usuários (Login)
CREATE TABLE usuario (
    id_usuario BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    tipo_usuario VARCHAR(20) NOT NULL,
    cidade VARCHAR(100),
    estado VARCHAR(100)
);

-- Tabela Perfil Atleta (Adicionadas as colunas que faltavam)
CREATE TABLE perfil_atleta (
    id_perfil_atleta BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL UNIQUE,
    idade INTEGER,
    modalidade VARCHAR(100),
    posicao VARCHAR(100),
    altura DECIMAL(3,2),
    peso DECIMAL(5,2),
    data_nascimento DATE,
    telefone_contato VARCHAR(20),
    observacoes TEXT,
    midiakit_url VARCHAR(255),
    competicoes_titulos TEXT,
    redes_social VARCHAR(255),
    historico TEXT,
    CONSTRAINT fk_perfil_atleta_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- Tabela Perfil Marca
CREATE TABLE perfil_marca (
    id_perfil_marca BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL UNIQUE,
    produto VARCHAR(255),
    tempo_mercado INTEGER,
    atletas_patrocinados TEXT,
    tipo_investimento VARCHAR(255),
    redes_social VARCHAR(255),
    CONSTRAINT fk_perfil_marca_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- Tabela de Match
CREATE TABLE match_table (
    id_match BIGSERIAL PRIMARY KEY,
    id_usuario_a BIGINT NOT NULL,
    id_usuario_b BIGINT NOT NULL,
    tipo_match VARCHAR(20) NOT NULL,
    data_match TIMESTAMP NOT NULL,
    CONSTRAINT fk_match_usuario_a FOREIGN KEY (id_usuario_a) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_match_usuario_b FOREIGN KEY (id_usuario_b) REFERENCES usuario(id_usuario),
    UNIQUE (id_usuario_a, id_usuario_b)
);

-- Tabela de Mensagens
CREATE TABLE mensagem (
    id_mensagem BIGSERIAL PRIMARY KEY,
    id_match BIGINT NOT NULL,
    id_remetente BIGINT NOT NULL,
    texto TEXT NOT NULL,
    data_envio TIMESTAMP NOT NULL,
    lida BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_mensagem_match FOREIGN KEY (id_match) REFERENCES match_table(id_match),
    CONSTRAINT fk_mensagem_remetente FOREIGN KEY (id_remetente) REFERENCES usuario(id_usuario)
);

-- Tabela de Interesse
CREATE TABLE interesse (
    id_interesse BIGSERIAL PRIMARY KEY,
    id_origem BIGINT NOT NULL,
    id_destino BIGINT NOT NULL,
    tipo_interesse VARCHAR(50),
    data_envio TIMESTAMP NOT NULL,
    CONSTRAINT fk_interesse_origem FOREIGN KEY (id_origem) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_interesse_destino FOREIGN KEY (id_destino) REFERENCES usuario(id_usuario)
);