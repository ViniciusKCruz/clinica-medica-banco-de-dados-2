BEGIN;

CREATE TABLE IF NOT EXISTS staging.complemento_consulta (
    id_consulta BIGINT PRIMARY KEY,
    id_clinica BIGINT,
    id_medico_especialidade BIGINT,
    valor_bruto NUMERIC(12,2) CHECK (valor_bruto >= 0),
    valor_desconto NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (valor_desconto >= 0),
    solicitado_em TIMESTAMP,
    atendimento_iniciado_em TIMESTAMP,
    CHECK ((valor_bruto IS NULL AND valor_desconto = 0)
        OR (valor_bruto IS NOT NULL AND valor_desconto <= valor_bruto)),
    CHECK ((solicitado_em IS NULL AND atendimento_iniciado_em IS NULL)
        OR (solicitado_em IS NOT NULL AND atendimento_iniciado_em IS NOT NULL
            AND atendimento_iniciado_em >= solicitado_em))
);

CREATE TABLE IF NOT EXISTS dw.log_etl (
    id_execucao BIGSERIAL PRIMARY KEY,
    iniciado_em TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    finalizado_em TIMESTAMPTZ,
    pid INTEGER NOT NULL DEFAULT pg_backend_pid(),
    data_referencia DATE,
    status VARCHAR(15) NOT NULL CHECK (status IN ('EXECUTANDO', 'SUCESSO', 'ERRO')),
    qtd_linhas_antes BIGINT NOT NULL DEFAULT 0,
    qtd_linhas_depois BIGINT NOT NULL DEFAULT 0,
    qtd_linhas_antes_snowflake BIGINT NOT NULL DEFAULT 0,
    qtd_linhas_depois_snowflake BIGINT NOT NULL DEFAULT 0,
    qtd_consultas_origem BIGINT NOT NULL DEFAULT 0,
    qtd_inseridas BIGINT NOT NULL DEFAULT 0,
    qtd_inseridas_snowflake BIGINT NOT NULL DEFAULT 0,
    pacientes_versionados BIGINT NOT NULL DEFAULT 0,
    pacientes_versionados_snowflake BIGINT NOT NULL DEFAULT 0,
    ultimo_id_antes BIGINT NOT NULL DEFAULT 0,
    ultimo_id_depois BIGINT NOT NULL DEFAULT 0,
    ultimo_id_antes_snowflake BIGINT NOT NULL DEFAULT 0,
    ultimo_id_depois_snowflake BIGINT NOT NULL DEFAULT 0,
    contagens_antes JSONB NOT NULL DEFAULT '{}'::JSONB,
    contagens_depois JSONB NOT NULL DEFAULT '{}'::JSONB,
    sqlstate_erro VARCHAR(5),
    mensagem_erro TEXT
);

CREATE OR REPLACE FUNCTION dw.normalizar_texto(valor TEXT, padrao TEXT DEFAULT 'NÃO INFORMADO')
RETURNS TEXT LANGUAGE SQL IMMUTABLE AS $$
    SELECT COALESCE(NULLIF(UPPER(TRIM(valor)), ''), padrao);
$$;

CREATE OR REPLACE FUNCTION dw.contagens_etl() RETURNS JSONB LANGUAGE plpgsql AS $$
DECLARE
    tabela RECORD;
    quantidade BIGINT;
    resultado JSONB := '{}'::JSONB;
BEGIN
    FOR tabela IN
        SELECT table_schema, table_name FROM information_schema.tables
        WHERE table_type = 'BASE TABLE'
          AND (table_schema = 'staging'
            OR (table_schema IN ('dw', 'dw_snowflake')
                AND (table_name LIKE 'dim\_%' ESCAPE '\' OR table_name = 'fato_consulta')))
        ORDER BY table_schema, table_name
    LOOP
        EXECUTE format('SELECT COUNT(*) FROM %I.%I', tabela.table_schema, tabela.table_name)
            INTO quantidade;
        resultado := resultado || jsonb_build_object(tabela.table_schema || '.' || tabela.table_name, quantidade);
    END LOOP;
    RETURN resultado;
END;
$$;

CREATE OR REPLACE PROCEDURE dw.executar_etl(p_data_referencia DATE DEFAULT CURRENT_DATE)
LANGUAGE plpgsql AS $$
DECLARE
    v_execucao BIGINT;
    v_esquema TEXT;
    v_contagens JSONB := '{}'::JSONB;
    v_ultimo BIGINT;
    v_ultimo_snow BIGINT;
    v_inicio DATE;
    v_fim DATE;
    v_quantidade BIGINT;
    v_coluna_convenio TEXT;
    v_valor_convenio TEXT;
    v_campos_convenio TEXT;
    v_valores_convenio TEXT;
    v_erro TEXT;
    v_estado TEXT;
BEGIN
    -- Fora do subbloco: em caso de falha, o registro de auditoria sobrevive ao rollback da carga.
    INSERT INTO dw.log_etl (data_referencia, status) VALUES (p_data_referencia, 'EXECUTANDO')
        RETURNING id_execucao INTO v_execucao;
    BEGIN
        IF NOT pg_try_advisory_xact_lock(26611, 2) THEN
            v_contagens := dw.contagens_etl();
            SELECT COALESCE(MAX(id_consulta_oltp), 0) INTO v_ultimo FROM dw.fato_consulta;
            SELECT COALESCE(MAX(id_consulta_oltp), 0) INTO v_ultimo_snow FROM dw_snowflake.fato_consulta;
            RAISE EXCEPTION 'Já existe uma execução do ETL em andamento';
        END IF;
        v_contagens := dw.contagens_etl();
        SELECT COALESCE(MAX(id_consulta_oltp), 0) INTO v_ultimo FROM dw.fato_consulta;
        SELECT COALESCE(MAX(id_consulta_oltp), 0) INTO v_ultimo_snow FROM dw_snowflake.fato_consulta;
        IF p_data_referencia IS NULL THEN
            RAISE EXCEPTION 'A data de referência não pode ser nula';
        END IF;
        IF EXISTS (SELECT 1 FROM dw.log_etl WHERE status = 'SUCESSO'
                   AND data_referencia > p_data_referencia) THEN
            RAISE EXCEPTION 'A data de referência não pode retroceder após uma carga bem-sucedida';
        END IF;

        -- Mantém uma fotografia consistente entre as nove extrações, mesmo em READ COMMITTED.
        -- Escritas nessas fontes aguardam somente até o fim da transação da carga.
        LOCK TABLE public.pessoa, public.paciente, public.medico, public.convenio,
            public.medico_especialidade, public.clinica, public.consulta,
            public.prontuario, public.fila_espera_consulta, staging.complemento_consulta IN SHARE MODE;
        UPDATE dw.log_etl SET contagens_antes = v_contagens,
            qtd_linhas_antes = (v_contagens ->> 'dw.fato_consulta')::BIGINT,
            qtd_linhas_antes_snowflake = (v_contagens ->> 'dw_snowflake.fato_consulta')::BIGINT,
            ultimo_id_antes = v_ultimo, ultimo_id_antes_snowflake = v_ultimo_snow
        WHERE id_execucao = v_execucao;

        TRUNCATE staging.pessoa, staging.paciente, staging.medico, staging.convenio,
            staging.medico_especialidade, staging.clinica, staging.consulta,
            staging.prontuario, staging.fila_espera_consulta;
        INSERT INTO staging.pessoa (id, nome, cpf, endereco, bairro, cidade, uf)
            SELECT id, nome, cpf, endereco, bairro, cidade, uf FROM public.pessoa;
        INSERT INTO staging.paciente (id_paciente, status_paciente, id_convenio)
            SELECT id_paciente, status_paciente, id_convenio FROM public.paciente;
        INSERT INTO staging.medico (id_medico, telefone, status_atual, salario)
            SELECT id_medico, telefone, status_atual, salario FROM public.medico;
        INSERT INTO staging.convenio (id_convenio, nome_convenio, cnpj, telefone_contato)
            SELECT id_convenio, nome_convenio, cnpj, telefone_contato FROM public.convenio;
        INSERT INTO staging.medico_especialidade (id_medico_especialidade, nome, descricao, id_medico)
            SELECT id_medico_especialidade, nome, descricao, id_medico FROM public.medico_especialidade;
        INSERT INTO staging.clinica (id_clinica, endereco, bairro, cidade, uf, id_paciente, id_medico)
            SELECT id_clinica, endereco, bairro, cidade, uf, id_paciente, id_medico FROM public.clinica;
        INSERT INTO staging.consulta (id_consulta, nome, horario, status_consulta, id_medico, id_paciente)
            SELECT id_consulta, nome, horario, status_consulta, id_medico, id_paciente FROM public.consulta;
        INSERT INTO staging.prontuario (id_prontuario, id_consulta, id_paciente, id_medico,
            queixa_principal, diagnostico, prescricao_medica, data_registro)
            SELECT id_prontuario, id_consulta, id_paciente, id_medico,
                queixa_principal, diagnostico, prescricao_medica, data_registro FROM public.prontuario;
        INSERT INTO staging.fila_espera_consulta (id_fila_espera_consulta, data_solicitacao,
            status_fila, prioridade, id_paciente, id_medico)
            SELECT id_fila_espera_consulta, data_solicitacao, status_fila, prioridade,
                id_paciente, id_medico FROM public.fila_espera_consulta;

        -- Tabelas temporárias são renovadas também em chamadas na mesma transação.
        IF to_regclass('pg_temp.etl_paciente') IS NOT NULL THEN
            DROP TABLE pg_temp.etl_paciente, pg_temp.etl_medico,
                pg_temp.etl_especialidade, pg_temp.etl_consulta;
        END IF;
        CREATE TEMP TABLE etl_paciente ON COMMIT DROP AS
            SELECT pa.id_paciente, dw.normalizar_texto(pe.nome) AS nome,
                NULLIF(TRIM(pe.cpf), '') AS cpf,
                dw.normalizar_texto(pa.status_paciente) AS status,
                dw.normalizar_texto(pe.cidade) AS cidade,
                dw.normalizar_texto(pe.uf, 'NI') AS uf,
                pa.id_convenio,
                CASE WHEN pa.id_convenio IS NULL THEN 'PARTICULAR'
                    ELSE dw.normalizar_texto(co.nome_convenio) END AS nome_convenio,
                LEAST(p_data_referencia, COALESCE(datas.primeira_data, p_data_referencia)) AS data_inicio
            FROM staging.paciente pa JOIN staging.pessoa pe ON pe.id = pa.id_paciente
            LEFT JOIN staging.convenio co ON co.id_convenio = pa.id_convenio
            LEFT JOIN (SELECT id_paciente, MIN(horario::DATE) AS primeira_data
                       FROM staging.consulta GROUP BY id_paciente) datas ON datas.id_paciente = pa.id_paciente;
        CREATE UNIQUE INDEX ON etl_paciente (id_paciente);
        CREATE TEMP TABLE etl_medico ON COMMIT DROP AS
            SELECT me.id_medico, dw.normalizar_texto(pe.nome) AS nome,
                dw.normalizar_texto(pe.cidade) AS cidade, dw.normalizar_texto(pe.uf, 'NI') AS uf
            FROM staging.medico me JOIN staging.pessoa pe ON pe.id = me.id_medico;
        CREATE TEMP TABLE etl_especialidade ON COMMIT DROP AS
            SELECT DISTINCT id_medico, dw.normalizar_texto(nome, 'NÃO INFORMADA') AS especialidade
            FROM staging.medico_especialidade
            UNION SELECT id_medico, 'NÃO INFORMADA' FROM etl_medico;

        IF EXISTS (
            SELECT 1 FROM staging.complemento_consulta x
            JOIN staging.consulta c ON c.id_consulta = x.id_consulta
            LEFT JOIN staging.clinica cl ON cl.id_clinica = x.id_clinica
            LEFT JOIN staging.medico_especialidade e ON e.id_medico_especialidade = x.id_medico_especialidade
            WHERE c.id_consulta > LEAST(v_ultimo, v_ultimo_snow)
              AND ((x.id_clinica IS NOT NULL AND cl.id_clinica IS NULL)
                OR (x.id_medico_especialidade IS NOT NULL
                    AND (e.id_medico_especialidade IS NULL OR e.id_medico IS DISTINCT FROM c.id_medico)))
        ) THEN
            RAISE EXCEPTION 'Complemento inválido: clínica inexistente ou especialidade incompatível com o médico';
        END IF;
        CREATE TEMP TABLE etl_consulta ON COMMIT DROP AS
            SELECT c.id_consulta, c.horario, c.id_paciente, c.id_medico,
                dw.normalizar_texto(c.status_consulta) AS status_consulta,
                x.id_clinica,
                CASE WHEN x.id_medico_especialidade IS NOT NULL
                    THEN dw.normalizar_texto(e.nome, 'NÃO INFORMADA')
                    ELSE COALESCE(inferida.especialidade, 'NÃO INFORMADA') END AS especialidade,
                x.valor_bruto,
                CASE WHEN x.valor_bruto IS NOT NULL THEN x.valor_desconto END AS valor_desconto,
                x.valor_bruto - x.valor_desconto AS valor_liquido,
                ROUND(EXTRACT(EPOCH FROM (x.atendimento_iniciado_em - x.solicitado_em)) / 60, 2)
                    AS tempo_espera_minutos
            FROM staging.consulta c
            LEFT JOIN staging.complemento_consulta x ON x.id_consulta = c.id_consulta
            LEFT JOIN staging.medico_especialidade e ON e.id_medico_especialidade = x.id_medico_especialidade
            LEFT JOIN LATERAL (
                -- Somente correspondência completa e única entre os nomes padronizados.
                -- Não usa prefixos truncados nem multiplica consultas por especialidade.
                SELECT MIN(s.especialidade) AS especialidade FROM etl_especialidade s
                WHERE s.id_medico = c.id_medico
                  AND dw.normalizar_texto(c.nome) = 'CONSULTA DE ' || s.especialidade
                HAVING COUNT(*) = 1
            ) inferida ON TRUE
            WHERE c.id_consulta > LEAST(v_ultimo, v_ultimo_snow);
        CREATE UNIQUE INDEX ON etl_consulta (id_consulta);

        -- Cinco anos civis (dois anteriores, atual e dois seguintes); amplia para datas da origem.
        SELECT LEAST(make_date(EXTRACT(YEAR FROM p_data_referencia)::INTEGER - 2, 1, 1), MIN(horario::DATE)),
            GREATEST(make_date(EXTRACT(YEAR FROM p_data_referencia)::INTEGER + 3, 1, 1) - 1, MAX(horario::DATE))
            INTO v_inicio, v_fim FROM staging.consulta;

        -- Convênio normalizado, incluindo PARTICULAR como membro reservado de SK 0.
        INSERT INTO dw_snowflake.dim_convenio (sk_convenio, nome_convenio)
            VALUES (0, 'PARTICULAR') ON CONFLICT (sk_convenio) DO NOTHING;
        INSERT INTO dw_snowflake.dim_convenio (id_convenio_oltp, nome_convenio)
            SELECT id_convenio, dw.normalizar_texto(nome_convenio) FROM staging.convenio
            ON CONFLICT (id_convenio_oltp) DO UPDATE SET nome_convenio = EXCLUDED.nome_convenio;
        INSERT INTO dw_snowflake.dim_especialidade (sk_especialidade, nome)
            VALUES (0, 'NÃO INFORMADA') ON CONFLICT (sk_especialidade) DO NOTHING;
        INSERT INTO dw_snowflake.dim_especialidade (nome)
            SELECT DISTINCT especialidade FROM etl_especialidade ON CONFLICT (nome) DO NOTHING;

        FOREACH v_esquema IN ARRAY ARRAY['dw', 'dw_snowflake'] LOOP
            -- SK 0 é reservada para membros sem identificação; IDs reais continuam em SERIAL.
            EXECUTE format('INSERT INTO %I.dim_tempo VALUES (0, DATE ''1900-01-01'', 1, 1, 1, 1, 1900, 1)
                ON CONFLICT (sk_tempo) DO NOTHING', v_esquema);
            EXECUTE format('INSERT INTO %I.dim_tempo (data_completa, dia, mes, trimestre, semestre, ano, dia_semana)
                SELECT d::DATE, EXTRACT(DAY FROM d), EXTRACT(MONTH FROM d), EXTRACT(QUARTER FROM d),
                    CASE WHEN EXTRACT(MONTH FROM d) <= 6 THEN 1 ELSE 2 END,
                    EXTRACT(YEAR FROM d), EXTRACT(ISODOW FROM d)
                FROM generate_series($1::TIMESTAMP, $2::TIMESTAMP, INTERVAL ''1 day'') d
                ON CONFLICT (data_completa) DO NOTHING', v_esquema) USING v_inicio, v_fim;
            EXECUTE format('INSERT INTO %I.dim_clinica (sk_clinica, bairro, cidade, uf)
                VALUES (0, ''NÃO INFORMADO'', ''NÃO INFORMADA'', ''NI'')
                ON CONFLICT (sk_clinica) DO NOTHING', v_esquema);
            EXECUTE format('INSERT INTO %I.dim_clinica (id_clinica_oltp, bairro, cidade, uf)
                SELECT id_clinica, dw.normalizar_texto(bairro), dw.normalizar_texto(cidade),
                    dw.normalizar_texto(uf, ''NI'') FROM staging.clinica
                ON CONFLICT (id_clinica_oltp) DO UPDATE SET bairro = EXCLUDED.bairro,
                    cidade = EXCLUDED.cidade, uf = EXCLUDED.uf', v_esquema);

            IF v_esquema = 'dw' THEN
                v_coluna_convenio := 'd.id_convenio_oltp';
                v_valor_convenio := 'p.id_convenio';
                v_campos_convenio := 'id_convenio_oltp, nome_convenio';
                v_valores_convenio := 'p.id_convenio, p.nome_convenio';
                EXECUTE 'INSERT INTO dw.dim_paciente (sk_paciente, nome, status, cidade, uf,
                    nome_convenio, data_inicio) VALUES (0, ''NÃO INFORMADO'', ''NÃO INFORMADO'',
                    ''NÃO INFORMADA'', ''NI'', ''NÃO INFORMADO'', DATE ''1900-01-01'')
                    ON CONFLICT (sk_paciente) DO NOTHING';
                EXECUTE 'INSERT INTO dw.dim_medico (sk_medico, nome, especialidade, cidade, uf)
                    VALUES (0, ''NÃO INFORMADO'', ''NÃO INFORMADA'', ''NÃO INFORMADA'', ''NI'')
                    ON CONFLICT (sk_medico) DO NOTHING';
                INSERT INTO dw.dim_medico (id_medico_oltp, nome, especialidade, cidade, uf)
                    SELECT m.id_medico, m.nome, s.especialidade, m.cidade, m.uf
                    FROM etl_medico m JOIN etl_especialidade s ON s.id_medico = m.id_medico
                    ON CONFLICT (id_medico_oltp, especialidade) DO UPDATE SET nome = EXCLUDED.nome,
                        cidade = EXCLUDED.cidade, uf = EXCLUDED.uf;
            ELSE
                v_coluna_convenio := 'd.sk_convenio';
                v_valor_convenio := 'COALESCE(cv.sk_convenio, 0)';
                v_campos_convenio := 'sk_convenio';
                v_valores_convenio := 'COALESCE(cv.sk_convenio, 0)';
                EXECUTE 'INSERT INTO dw_snowflake.dim_paciente (sk_paciente, nome, status, cidade, uf,
                    sk_convenio, data_inicio) VALUES (0, ''NÃO INFORMADO'', ''NÃO INFORMADO'',
                    ''NÃO INFORMADA'', ''NI'', 0, DATE ''1900-01-01'')
                    ON CONFLICT (sk_paciente) DO NOTHING';
                EXECUTE 'INSERT INTO dw_snowflake.dim_medico (sk_medico, nome, sk_especialidade, cidade, uf)
                    VALUES (0, ''NÃO INFORMADO'', 0, ''NÃO INFORMADA'', ''NI'')
                    ON CONFLICT (sk_medico) DO NOTHING';
                INSERT INTO dw_snowflake.dim_medico (id_medico_oltp, nome, sk_especialidade, cidade, uf)
                    SELECT m.id_medico, m.nome, s.sk_especialidade, m.cidade, m.uf
                    FROM etl_medico m JOIN etl_especialidade e ON e.id_medico = m.id_medico
                    JOIN dw_snowflake.dim_especialidade s ON s.nome = e.especialidade
                    ON CONFLICT (id_medico_oltp, sk_especialidade) DO UPDATE SET nome = EXCLUDED.nome,
                        cidade = EXCLUDED.cidade, uf = EXCLUDED.uf;
            END IF;

            -- SCD 2: fecha somente mudanças de cidade/UF ou convênio, inclusive nomes iguais.
            EXECUTE format('UPDATE %I.dim_paciente d SET atual = FALSE, data_fim = $1
                FROM etl_paciente p LEFT JOIN dw_snowflake.dim_convenio cv ON cv.id_convenio_oltp = p.id_convenio
                WHERE d.id_paciente_oltp = p.id_paciente AND d.atual
                  AND (d.cidade, d.uf, %s) IS DISTINCT FROM (p.cidade, p.uf, %s)',
                v_esquema, v_coluna_convenio, v_valor_convenio) USING p_data_referencia;
            GET DIAGNOSTICS v_quantidade = ROW_COUNT;
            IF v_esquema = 'dw' THEN
                UPDATE dw.log_etl SET pacientes_versionados = v_quantidade WHERE id_execucao = v_execucao;
            ELSE
                UPDATE dw.log_etl SET pacientes_versionados_snowflake = v_quantidade WHERE id_execucao = v_execucao;
            END IF;
            EXECUTE format('INSERT INTO %I.dim_paciente (id_paciente_oltp, nome, cpf, status,
                    cidade, uf, %s, data_inicio)
                SELECT p.id_paciente, p.nome, p.cpf, p.status, p.cidade, p.uf, %s,
                    CASE WHEN EXISTS (SELECT 1 FROM %I.dim_paciente h WHERE h.id_paciente_oltp = p.id_paciente)
                        THEN $1 ELSE p.data_inicio END
                FROM etl_paciente p LEFT JOIN dw_snowflake.dim_convenio cv ON cv.id_convenio_oltp = p.id_convenio
                WHERE NOT EXISTS (SELECT 1 FROM %I.dim_paciente d
                    WHERE d.id_paciente_oltp = p.id_paciente AND d.atual)',
                v_esquema, v_campos_convenio, v_valores_convenio, v_esquema, v_esquema) USING p_data_referencia;
            EXECUTE format('UPDATE %I.dim_paciente d SET nome = p.nome, cpf = p.cpf, status = p.status
                FROM etl_paciente p WHERE d.id_paciente_oltp = p.id_paciente AND d.atual', v_esquema);
            IF v_esquema = 'dw' THEN
                -- Correção Tipo 1 do rótulo do convênio, igual ao floco, inclusive no histórico.
                UPDATE dw.dim_paciente d SET nome_convenio = dw.normalizar_texto(c.nome_convenio)
                    FROM staging.convenio c WHERE d.id_convenio_oltp = c.id_convenio;
            END IF;

            -- Consulta tardia usa a versão vigente na data do evento. Antes do primeiro
            -- histórico conhecido, usa a versão mais antiga disponível (estado inicial assumido).
            EXECUTE format('INSERT INTO %I.fato_consulta (id_consulta_oltp, sk_tempo, sk_paciente,
                    sk_medico, sk_clinica, status_consulta, qtd_consultas, valor_bruto,
                    valor_desconto, valor_liquido, tempo_espera_minutos)
                SELECT c.id_consulta, t.sk_tempo, COALESCE(p.sk_paciente, 0), COALESCE(m.sk_medico, 0),
                    COALESCE(cl.sk_clinica, 0), c.status_consulta, 1, c.valor_bruto,
                    c.valor_desconto, c.valor_liquido, c.tempo_espera_minutos
                FROM etl_consulta c
                JOIN %I.dim_tempo t ON t.data_completa = COALESCE(c.horario::DATE, DATE ''1900-01-01'')
                LEFT JOIN LATERAL (
                    SELECT h.sk_paciente FROM %I.dim_paciente h WHERE h.id_paciente_oltp = c.id_paciente
                    ORDER BY (h.data_inicio <= COALESCE(c.horario::DATE, $2)) DESC,
                        CASE WHEN h.data_inicio <= COALESCE(c.horario::DATE, $2) THEN h.data_inicio END DESC,
                        h.data_inicio ASC, h.sk_paciente DESC LIMIT 1
                ) p ON TRUE
                LEFT JOIN %I.dim_medico m ON m.id_medico_oltp = c.id_medico AND %s
                LEFT JOIN %I.dim_clinica cl ON cl.id_clinica_oltp = c.id_clinica
                WHERE c.id_consulta > $1',
                v_esquema, v_esquema, v_esquema, v_esquema,
                CASE WHEN v_esquema = 'dw' THEN 'm.especialidade = c.especialidade'
                    ELSE 'm.sk_especialidade = (SELECT s.sk_especialidade FROM dw_snowflake.dim_especialidade s WHERE s.nome = c.especialidade)' END,
                v_esquema)
                USING CASE WHEN v_esquema = 'dw' THEN v_ultimo ELSE v_ultimo_snow END, p_data_referencia;
            GET DIAGNOSTICS v_quantidade = ROW_COUNT;
            IF v_esquema = 'dw' THEN
                UPDATE dw.log_etl SET qtd_inseridas = v_quantidade WHERE id_execucao = v_execucao;
            ELSE
                UPDATE dw.log_etl SET qtd_inseridas_snowflake = v_quantidade WHERE id_execucao = v_execucao;
            END IF;
        END LOOP;

        UPDATE dw.log_etl SET status = 'SUCESSO', finalizado_em = clock_timestamp(),
            qtd_linhas_depois = (SELECT COUNT(*) FROM dw.fato_consulta),
            qtd_linhas_depois_snowflake = (SELECT COUNT(*) FROM dw_snowflake.fato_consulta),
            qtd_consultas_origem = (SELECT COUNT(*) FROM staging.consulta),
            ultimo_id_depois = COALESCE((SELECT MAX(id_consulta_oltp) FROM dw.fato_consulta), 0),
            ultimo_id_depois_snowflake = COALESCE((SELECT MAX(id_consulta_oltp) FROM dw_snowflake.fato_consulta), 0),
            contagens_depois = dw.contagens_etl()
        WHERE id_execucao = v_execucao;
        RAISE NOTICE 'ETL % concluído. Consulte dw.log_etl.', v_execucao;
    EXCEPTION WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_erro = MESSAGE_TEXT, v_estado = RETURNED_SQLSTATE;
        -- O subbloco desfez staging, dimensões e fatos dos DOIS modelos.
        -- Não relança o erro: assim o CALL em autocommit pode persistir a auditoria da falha.
        UPDATE dw.log_etl SET status = 'ERRO', finalizado_em = clock_timestamp(),
            sqlstate_erro = v_estado, mensagem_erro = v_erro,
            contagens_antes = v_contagens, contagens_depois = v_contagens,
            qtd_linhas_antes = COALESCE((v_contagens ->> 'dw.fato_consulta')::BIGINT, 0),
            qtd_linhas_depois = COALESCE((v_contagens ->> 'dw.fato_consulta')::BIGINT, 0),
            qtd_linhas_antes_snowflake = COALESCE((v_contagens ->> 'dw_snowflake.fato_consulta')::BIGINT, 0),
            qtd_linhas_depois_snowflake = COALESCE((v_contagens ->> 'dw_snowflake.fato_consulta')::BIGINT, 0),
            ultimo_id_antes = COALESCE(v_ultimo, 0), ultimo_id_depois = COALESCE(v_ultimo, 0),
            ultimo_id_antes_snowflake = COALESCE(v_ultimo_snow, 0), ultimo_id_depois_snowflake = COALESCE(v_ultimo_snow, 0)
        WHERE id_execucao = v_execucao;
        RAISE WARNING 'ETL % falhou [%]: %. Carga desfeita; consulte dw.log_etl.', v_execucao, v_estado, v_erro;
    END;
END;
$$;

COMMENT ON TABLE staging.complemento_consulta IS 'Vínculos e medidas opcionais por consulta; fonte explícita preservada entre cargas. Preencher antes da primeira carga do evento.';
COMMENT ON TABLE dw.log_etl IS 'Auditoria das cargas dos dois modelos; contagens principais representam linhas da fato, JSON contém contagens por tabela.';
COMMENT ON PROCEDURE dw.executar_etl(DATE) IS 'Carga atômica estrela/floco, incremental por ID, com SCD 2. Consultar status no log; erros da carga são registrados sem relançamento.';
COMMIT;
