# Sistema de Consulta Médica

Projeto Maven para Eclipse com JPA/Hibernate, alinhado ao DER/UML de consulta médica.

## Escopo desta etapa

| Item | Conteúdo |
|------|----------|
| Maven / Eclipse | `pom.xml` (JPA 2.1, Hibernate 5.2.10.Final, PostgreSQL) |
| `persistence.xml` | Driver JDBC, dialeto SQL e credenciais |
| Herança | `Pessoa` (abstrata, `@Inheritance JOINED`) → `Paciente`, `Medico` |
| Embeddable | `Endereco` (`endereco`, `bairro`, `cidade`, `uf`) |
| Enums | `StatusMedico`, `StatusPaciente`, `StatusConsulta`, `StatusFila`, `Prioridade` |

## Estrutura

```
sistema-consulta-medica/
├── pom.xml
├── README.md
└── src/main/
    ├── java/br/com/sistemaconsulta/
    │   ├── App.java
    │   ├── model/
    │   │   ├── Pessoa.java
    │   │   ├── Paciente.java
    │   │   ├── Medico.java
    │   │   ├── Endereco.java
    │   │   └── enums/
    │   └── util/JpaUtil.java
    └── resources/META-INF/persistence.xml
```

## Importar no Eclipse

1. **File → Import… → Maven → Existing Maven Projects**
2. Selecione a pasta `sistema-consulta-medica`
3. Confirme o `pom.xml` → **Finish**
4. Aguarde o download das dependências

Requisito: JDK 8 e m2e (Maven Integration for Eclipse).

## Configurar PostgreSQL

1. Crie o database (pgAdmin ou `psql`):

```sql
CREATE DATABASE sistema_consulta_medica;
```

2. Copie `.env.example` para `.env` na raiz e preencha `DB_URL`, `DB_USER` e `DB_PASSWORD`. Variáveis de ambiente têm prioridade. O `.env` não é versionado; use a raiz do projeto como diretório de execução.

Unidades:

- **`sistemaConsultaPU`** — PostgreSQL (padrão)

## Validar

Eclipse: `App` → **Run As → Java Application**

```bash
mvn -q compile exec:java
```
## Modelagem

O diagrama na linguagem do dbdiagram.io está em [docs/modelo-oltp.dbml](docs/modelo-oltp.dbml).
