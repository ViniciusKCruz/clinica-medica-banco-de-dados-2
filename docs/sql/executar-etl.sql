-- Executar com psql, sem envolver o arquivo em uma transação externa (-1).
\set ON_ERROR_STOP on
CALL dw.executar_etl();

SELECT id_execucao, status, qtd_linhas_antes, qtd_linhas_depois,
    qtd_inseridas, qtd_inseridas_snowflake, pacientes_versionados,
    pacientes_versionados_snowflake, iniciado_em, finalizado_em, mensagem_erro
FROM dw.log_etl WHERE pid = pg_backend_pid() ORDER BY id_execucao DESC LIMIT 1;

-- O CALL anterior já foi commitado: reporta falha ao shell preservando seu log.
DO $$
BEGIN
    IF (SELECT status FROM dw.log_etl WHERE pid = pg_backend_pid()
        ORDER BY id_execucao DESC LIMIT 1) IS DISTINCT FROM 'SUCESSO' THEN
        RAISE EXCEPTION 'Carga ETL falhou. Consulte dw.log_etl para obter a causa.';
    END IF;
END;
$$;
