# Sistema de Consulta Médica

Projeto Maven para Eclipse com JPA/Hibernate, alinhado ao DER/UML de consulta médica.

## Escopo desta etapa

| Item | Conteúdo |
|------|----------|
| Maven / Eclipse | `pom.xml` (Hibernate 5.6, PostgreSQL, H2) |
| `persistence.xml` | Driver JDBC, dialeto SQL e credenciais |
| Herança | `Pessoa` (abstrata, `@Inheritance JOINED`) → `Paciente`, `Medico` |
| Embeddable | `Endereco` (`endereco`, `bairro`, `cidade`, `uf`) |
| Enums | `StatusMedico`, `StatusPaciente`, `StatusConsulta`, `StatusFila`, `Prioridade` |

> Consulta, Prontuario, Convenio, Clinica, FilaEspera e MedicoEspecialidade ficam para as próximas pessoas/etapas.

## Estrutura

```
sistema-consulta-medica/
├── pom.xml
├── README.md
└── src/main/
    ├── java/br/com/sistemaconsulta/
    │   ├── AppSmokeTest.java
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

Requisito: JDK 11+ e m2e (Maven Integration for Eclipse).

## Configurar PostgreSQL

1. Crie o database (pgAdmin ou `psql`):

```sql
CREATE DATABASE sistema_consulta_medica;
```

2. Edite `src/main/resources/META-INF/persistence.xml`:

- `javax.persistence.jdbc.url` → `jdbc:postgresql://localhost:5432/sistema_consulta_medica`
- `javax.persistence.jdbc.user` / `password` (padrão no arquivo: `postgres` / `postgres`)

Unidades:

- **`sistemaConsultaPU`** — PostgreSQL (padrão)
- **`sistemaConsultaH2PU`** — H2 em memória (teste sem PostgreSQL)

## Validar

Eclipse: `AppSmokeTest` → **Run As → Java Application**

```bash
mvn -q compile exec:java
```

Saída esperada: `=== Etapa 1 OK (Consulta Médica) ===`
