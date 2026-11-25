# Modelagem e ETL da clínica médica

**Primeira etapa:** foram criados os schemas `staging`, `dw` (estrela) e
`dw_snowflake` (floco de neve). A fato registra uma consulta por linha, ligada às
dimensões Tempo, Paciente, Médico e Clínica, com quantidade, valor bruto, desconto,
valor líquido e tempo de espera. O floco separa Convênio e Especialidade em tabelas
próprias. Os modelos estão em [estrela](modelo-estrela.dbml) e
[floco de neve](modelo-floco-neve.dbml), com seus DDLs em `docs/sql`.

**Segunda etapa:** o ETL copia os dados do OLTP para `staging`, limpa os textos e carrega os modelos
estrela e floco de neve. Gera um calendário de cinco anos, insere novas consultas
por ID, mantém o histórico do paciente com SCD Tipo 2 e registra contagens e erros
em `dw.log_etl`. Se houver falha, desfaz a carga dos dois modelos.

Valores financeiros e espera são calculados quando informados em
`staging.complemento_consulta`; caso contrário, ficam nulos.

Na primeira instalação, com o OLTP criado, execute no `psql` a partir da raiz do projeto:

```sql
\i docs/sql/staging.sql
\i docs/sql/dw-estrela.sql
\i docs/sql/dw-floco-neve.sql
\i docs/sql/etl.sql
\i docs/sql/executar-etl.sql
```

Se os schemas já existem, comece por `etl.sql`. Nas próximas cargas, execute apenas
`executar-etl.sql`.
