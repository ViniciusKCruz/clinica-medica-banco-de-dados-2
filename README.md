# Sistema de Consulta Médica

Sistema OLTP em Java 8 com interface console, JPA 2.1, Hibernate 5.2.10.Final e PostgreSQL.

## Requisitos

- JDK 8, Maven e PostgreSQL.
- Para importar no Eclipse: **File → Import → Maven → Existing Maven Projects**.

## Configuração e execução

1. Crie o banco:

```sql
CREATE DATABASE sistema_consulta_medica;
```

2. Copie a configuração de exemplo:

```bash
cp .env.example .env
```

3. Preencha `DB_URL`, `DB_USER` e `DB_PASSWORD` no `.env`. Variáveis de ambiente têm prioridade.
4. Execute na raiz do projeto:

```bash
mvn -q compile exec:java
```

Na IDE, execute `br.com.sistemaconsulta.App` com JDK 8 e a raiz do projeto como diretório de trabalho.

A aplicação atualiza as tabelas e, se o banco estiver vazio, gera 200 pacientes, 100 especialidades,
5 clínicas e 2.000 consultas, além dos dados de apoio. Os novos endereços têm capital e UF coerentes.

## Funcionalidades

- **Pacientes:** CRUD, vínculo com convênio e buscas por nome ou convênio.
- **Médicos:** CRUD, resumo de salários por status e salários acima da média.
- **Consultas e relatórios:** buscas por status, período ou médico; filtros combinados; detalhes com
  prontuário; totais por status e por médico. Inclui consultas JPQL, NamedQuery e Criteria API.

Listagens e buscas usam páginas de 10 registros. Até 10 resultados são exibidos diretamente.
Na navegação: `1` avança, `2` retorna à página anterior e `0` volta ao menu. .

## Modelagem
[Modelo relacional OLTP](docs/modelo-oltp.dbml) · [DDL PostgreSQL](docs/sql/oltp.sql)
