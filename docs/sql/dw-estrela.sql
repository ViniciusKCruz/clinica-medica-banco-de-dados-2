BEGIN;
CREATE SCHEMA dw;

CREATE TABLE dw.dim_tempo (
    sk_tempo SERIAL PRIMARY KEY,
    data_completa DATE NOT NULL UNIQUE,
    dia SMALLINT NOT NULL CHECK (dia BETWEEN 1 AND 31),
    mes SMALLINT NOT NULL CHECK (mes BETWEEN 1 AND 12),
    trimestre SMALLINT NOT NULL CHECK (trimestre BETWEEN 1 AND 4),
    semestre SMALLINT NOT NULL CHECK (semestre BETWEEN 1 AND 2),
    ano SMALLINT NOT NULL,
    dia_semana SMALLINT NOT NULL CHECK (dia_semana BETWEEN 1 AND 7)
);

CREATE TABLE dw.dim_paciente (
    sk_paciente SERIAL PRIMARY KEY,
    id_paciente_oltp BIGINT,
    nome VARCHAR(45) NOT NULL,
    cpf VARCHAR(11),
    status VARCHAR(45) NOT NULL,
    cidade VARCHAR(45) NOT NULL,
    uf VARCHAR(2) NOT NULL,
    id_convenio_oltp BIGINT,
    nome_convenio VARCHAR(45) NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE,
    atual BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_paciente_vigencia CHECK (
        (atual AND data_fim IS NULL)
        OR (NOT atual AND data_fim IS NOT NULL AND data_fim >= data_inicio)
    )
);

CREATE UNIQUE INDEX uq_paciente_atual ON dw.dim_paciente (id_paciente_oltp) WHERE atual;
CREATE INDEX ix_paciente_historico ON dw.dim_paciente (id_paciente_oltp, data_inicio, data_fim);

CREATE TABLE dw.dim_medico (
    sk_medico SERIAL PRIMARY KEY,
    id_medico_oltp BIGINT,
    nome VARCHAR(45) NOT NULL,
    especialidade VARCHAR(45) NOT NULL,
    cidade VARCHAR(45) NOT NULL,
    uf VARCHAR(2) NOT NULL,
    UNIQUE (id_medico_oltp, especialidade)
);

CREATE TABLE dw.dim_clinica (
    sk_clinica SERIAL PRIMARY KEY,
    id_clinica_oltp BIGINT UNIQUE,
    bairro VARCHAR(45) NOT NULL,
    cidade VARCHAR(45) NOT NULL,
    uf VARCHAR(45) NOT NULL
);

CREATE TABLE dw.fato_consulta (
    id_consulta_oltp BIGINT PRIMARY KEY,
    sk_tempo INTEGER NOT NULL REFERENCES dw.dim_tempo (sk_tempo),
    sk_paciente INTEGER NOT NULL REFERENCES dw.dim_paciente (sk_paciente),
    sk_medico INTEGER NOT NULL REFERENCES dw.dim_medico (sk_medico),
    sk_clinica INTEGER NOT NULL REFERENCES dw.dim_clinica (sk_clinica),
    status_consulta VARCHAR(45) NOT NULL,
    qtd_consultas SMALLINT NOT NULL DEFAULT 1 CHECK (qtd_consultas = 1),
    valor_bruto NUMERIC(12,2) CHECK (valor_bruto >= 0),
    valor_desconto NUMERIC(12,2) CHECK (valor_desconto >= 0),
    valor_liquido NUMERIC(12,2) CHECK (valor_liquido >= 0),
    tempo_espera_minutos NUMERIC(12,2) CHECK (tempo_espera_minutos >= 0),
    CONSTRAINT ck_fato_valores CHECK (
        (valor_bruto IS NULL AND valor_desconto IS NULL AND valor_liquido IS NULL)
        OR (valor_bruto IS NOT NULL AND valor_desconto IS NOT NULL AND valor_liquido IS NOT NULL
            AND valor_desconto <= valor_bruto AND valor_liquido = valor_bruto - valor_desconto)
    )
);
CREATE INDEX ix_fato_tempo ON dw.fato_consulta (sk_tempo);
CREATE INDEX ix_fato_paciente ON dw.fato_consulta (sk_paciente);
CREATE INDEX ix_fato_medico ON dw.fato_consulta (sk_medico);
CREATE INDEX ix_fato_clinica ON dw.fato_consulta (sk_clinica);

COMMIT;
