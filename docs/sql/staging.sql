BEGIN;

CREATE SCHEMA staging;

CREATE TABLE staging.pessoa (
    id BIGINT, nome VARCHAR(45), cpf VARCHAR(11), endereco VARCHAR(45),
    bairro VARCHAR(45), cidade VARCHAR(45), uf VARCHAR(2),
    extraido_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE staging.paciente (
    id_paciente BIGINT, status_paciente VARCHAR(45), id_convenio BIGINT,
    extraido_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE staging.medico (
    id_medico BIGINT, telefone VARCHAR(20), status_atual VARCHAR(15), salario NUMERIC(10,2),
    extraido_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE staging.convenio (
    id_convenio BIGINT, nome_convenio VARCHAR(45), cnpj VARCHAR(14), telefone_contato VARCHAR(20),
    extraido_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE staging.medico_especialidade (
    id_medico_especialidade BIGINT, nome VARCHAR(45), descricao VARCHAR(150), id_medico BIGINT,
    extraido_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE staging.clinica (
    id_clinica BIGINT, endereco VARCHAR(45), bairro VARCHAR(45), cidade VARCHAR(45),
    uf VARCHAR(45), id_paciente BIGINT, id_medico BIGINT,
    extraido_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE staging.consulta (
    id_consulta BIGINT, nome VARCHAR(45), horario TIMESTAMP, status_consulta VARCHAR(45),
    id_medico BIGINT, id_paciente BIGINT,
    extraido_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE staging.prontuario (
    id_prontuario BIGINT, id_consulta BIGINT, id_paciente BIGINT, id_medico BIGINT,
    queixa_principal VARCHAR(200), diagnostico VARCHAR(200), prescricao_medica VARCHAR(400),
    data_registro TIMESTAMP,
    extraido_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE staging.fila_espera_consulta (
    id_fila_espera_consulta BIGINT, data_solicitacao TIMESTAMP, status_fila VARCHAR(20),
    prioridade VARCHAR(45), id_paciente BIGINT, id_medico BIGINT,
    extraido_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON SCHEMA staging IS 'Dados brutos de public; transformação e carga são responsabilidade do ETL.';
COMMIT;
