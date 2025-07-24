CREATE TABLE usuario (
    id_usuario BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    tipo_usuario ENUM('ATLETA', 'MARCA', 'ADMIN') NOT NULL,
    cidade VARCHAR(100),
    estado VARCHAR(100),
    PRIMARY KEY (id_usuario)
);

CREATE TABLE perfil_atleta (
    id_perfil_atleta BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL UNIQUE, -- UNIQUE pois um usuário só pode ter 1 perfil de atleta
    idade INT,
    altura DECIMAL(3,2), -- Ex: 1.80
    peso DECIMAL(5,2),   -- Ex: 75.50
    modalidade VARCHAR(100), -- Pode ser um ENUM no futuro se houver um conjunto fixo
    competicoes_titulos TEXT,
    redes_social VARCHAR(255),
    historico TEXT,
    PRIMARY KEY (id_perfil_atleta),
    CONSTRAINT fk_perfil_atleta_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE TABLE perfil_marca (
    id_perfil_marca BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL UNIQUE,
    produto VARCHAR(255),
    tempo_mercado VARCHAR(100), -- Pode ser INT de anos, ou texto descritivo
    atletas_patrocinados TEXT,
    tipo_investimento TEXT,
    redes_social VARCHAR(255),
    PRIMARY KEY (id_perfil_marca),
    CONSTRAINT fk_perfil_marca_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE TABLE interesse (
    id_interesse BIGINT NOT NULL AUTO_INCREMENT,
    id_origem BIGINT NOT NULL,
    id_destino BIGINT NOT NULL,
    tipo_interesse ENUM('CURTIR', 'SUPER_CURTIR') NOT NULL, -- Exemplo de tipos, ajuste conforme necessário
    data_envio DATETIME NOT NULL,
    PRIMARY KEY (id_interesse),
    CONSTRAINT fk_interesse_origem FOREIGN KEY (id_origem) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_interesse_destino FOREIGN KEY (id_destino) REFERENCES usuario(id_usuario)
);

CREATE TABLE match_table ( -- 'match' é palavra reservada em SQL, usando 'match_table'
    id_match BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario_a BIGINT NOT NULL,
    id_usuario_b BIGINT NOT NULL,
    tipo_match ENUM('RECIPROCO', 'SUPER_MATCH') NOT NULL, -- Exemplo de tipos
    data_match DATETIME NOT NULL,
    PRIMARY KEY (id_match),
    CONSTRAINT fk_match_usuario_a FOREIGN KEY (id_usuario_a) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_match_usuario_b FOREIGN KEY (id_usuario_b) REFERENCES usuario(id_usuario),
    UNIQUE (id_usuario_a, id_usuario_b) -- Garante que um par de match seja único, independente da ordem
);

CREATE TABLE mensagem (
    id_mensagem BIGINT NOT NULL AUTO_INCREMENT,
    id_match BIGINT NOT NULL,
    id_remetente BIGINT NOT NULL,
    texto TEXT NOT NULL,
    data_envio DATETIME NOT NULL,
    PRIMARY KEY (id_mensagem),
    CONSTRAINT fk_mensagem_match FOREIGN KEY (id_match) REFERENCES match_table(id_match),
    CONSTRAINT fk_mensagem_remetente FOREIGN KEY (id_remetente) REFERENCES usuario(id_usuario)
);