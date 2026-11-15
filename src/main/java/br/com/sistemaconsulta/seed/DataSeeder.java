package br.com.sistemaconsulta.seed;

import br.com.sistemaconsulta.model.*;
import br.com.sistemaconsulta.model.enums.*;
import br.com.sistemaconsulta.util.JpaUtil;
import net.datafaker.Faker;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public final class DataSeeder {
    private static final int CLINICAS = 5;
    private static final int ESPECIALIDADES = 100;
    private static final int PACIENTES = 200;
    private static final int CONSULTAS = 2000;
    private static final int MEDICOS = 25;
    private static final int CONVENIOS = 5;
    private final EntityManagerFactory factory;
    private final Random random;
    private final Faker faker;

    public DataSeeder() {
        this(JpaUtil.getEntityManagerFactory(), new Random());
    }

    public DataSeeder(EntityManagerFactory factory, Random random) {
        this.factory = Objects.requireNonNull(factory);
        this.random = Objects.requireNonNull(random);
        Locale locale = new Locale("pt", "BR");
        this.faker = new Faker(locale, random);
        this.faker.addUrl(locale, Objects.requireNonNull(DataSeeder.class.getResource("/faker/clinica.yml")));
    }

    public boolean popular() {
        EntityManager em = factory.createEntityManager();
        EntityTransaction transacao = em.getTransaction();
        try {
            transacao.begin();
            if (bancoPossuiDados(em)) {
                transacao.rollback();
                return false;
            }
            Set<String> cpfs = new HashSet<>(em.createQuery("SELECT p.cpf FROM Pessoa p", String.class).getResultList());
            List<Convenio> convenios = criarConvenios(em);
            List<Medico> medicos = criarMedicos(em, cpfs);
            List<Paciente> pacientes = criarPacientes(em, cpfs, convenios);
            criarClinicas(em, medicos, pacientes);
            criarEspecialidades(em, medicos);
            LocalDateTime referencia = LocalDateTime.now().withNano(0);
            criarConsultas(em, medicos, pacientes, referencia);
            criarFila(em, medicos, pacientes, referencia);
            transacao.commit();
            return true;
        } catch (RuntimeException e) {
            try {
                if (transacao.isActive()) {
                    transacao.rollback();
                }
            } catch (RuntimeException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private boolean bancoPossuiDados(EntityManager em) {
        String[] entidades = {"Pessoa", "Clinica", "Convenio", "MedicoEspecialidade",
                "Consulta", "Prontuario", "FilaEsperaConsulta"};
        for (String entidade : entidades) {
            if (!em.createQuery("SELECT e.id FROM " + entidade + " e", Long.class)
                    .setMaxResults(1).getResultList().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private List<Convenio> criarConvenios(EntityManager em) {
        List<Convenio> lista = new ArrayList<>();
        for (int i = 0; i < CONVENIOS; i++) {
            Convenio convenio = new Convenio();
            convenio.setNomeConvenio(limitar(faker.company().name(), 45));
            convenio.setCnpj(faker.cnpj().valid(false));
            convenio.setTelefoneContato(telefone());
            em.persist(convenio);
            lista.add(convenio);
        }
        return lista;
    }

    private List<Medico> criarMedicos(EntityManager em, Set<String> cpfs) {
        List<Medico> lista = new ArrayList<>();
        for (int i = 0; i < MEDICOS; i++) {
            Medico medico = new Medico(limitar(faker.name().fullName(), 45), cpfLivre(cpfs), endereco(),
                    telefone(), StatusMedico.ATIVO, BigDecimal.valueOf(faker.number().numberBetween(6000, 18001)));
            em.persist(medico);
            lista.add(medico);
        }
        return lista;
    }

    private List<Paciente> criarPacientes(EntityManager em, Set<String> cpfs, List<Convenio> convenios) {
        List<Paciente> lista = new ArrayList<>();
        for (int i = 0; i < PACIENTES; i++) {
            Paciente paciente = new Paciente(limitar(faker.name().fullName(), 45), cpfLivre(cpfs), endereco(),
                    StatusPaciente.ATIVO);
            paciente.setConvenio(sortear(convenios));
            em.persist(paciente);
            lista.add(paciente);
        }
        return lista;
    }

    private void criarClinicas(EntityManager em, List<Medico> medicos, List<Paciente> pacientes) {
        for (int i = 0; i < CLINICAS; i++) {
            Clinica clinica = new Clinica();
            clinica.setEndereco(endereco());
            clinica.setMedico(medicos.get(i));
            clinica.setPaciente(pacientes.get(i));
            em.persist(clinica);
        }
    }

    private void criarEspecialidades(EntityManager em, List<Medico> medicos) {
        for (int i = 0; i < ESPECIALIDADES; i++) {
            MedicoEspecialidade especialidade = new MedicoEspecialidade();
            String area = faker.resolve("clinica.especialidade");
            especialidade.setNome(limitar(area, 45));
            especialidade.setDescricao(limitar("Atendimento especializado em " + area + ".", 150));
            especialidade.setMedico(medicos.get(i % medicos.size()));
            em.persist(especialidade);
        }
    }

    private void criarConsultas(EntityManager em, List<Medico> medicos, List<Paciente> pacientes, LocalDateTime referencia) {
        for (int i = 0; i < CONSULTAS; i++) {
            Medico medico = sortear(medicos);
            String especialidade = sortear(medico.getEspecialidades()).getNome();
            Consulta consulta = new Consulta();
            consulta.setNome(limitar("Consulta de " + especialidade, 45));
            consulta.setHorario(horarioPassado(referencia));
            consulta.setStatusConsulta(random.nextInt(5) == 0 ? StatusConsulta.CANCELADA : StatusConsulta.CONCLUIDA);
            consulta.setMedico(medico);
            consulta.setPaciente(pacientes.get(i % pacientes.size()));
            em.persist(consulta);
            if (consulta.getStatusConsulta() == StatusConsulta.CONCLUIDA) {
                Prontuario prontuario = new Prontuario();
                prontuario.setConsulta(consulta);
                prontuario.setMedico(consulta.getMedico());
                prontuario.setPaciente(consulta.getPaciente());
                prontuario.setQueixaPrincipal(limitar(faker.resolve("clinica.queixa_principal"), 200));
                prontuario.setDiagnostico(limitar(faker.resolve("clinica.diagnostico"), 200));
                prontuario.setPrescricaoMedica(limitar(faker.resolve("clinica.prescricao_medica"), 400));
                prontuario.setDataRegistro(consulta.getHorario().plusMinutes(30));
                em.persist(prontuario);
            }
        }
    }

    private void criarFila(EntityManager em, List<Medico> medicos, List<Paciente> pacientes, LocalDateTime referencia) {
        for (int i = 0; i < 50; i++) {
            FilaEsperaConsulta fila = new FilaEsperaConsulta();
            fila.setDataSolicitacao(horarioPassado(referencia));
            fila.setStatusFila(random.nextBoolean() ? StatusFila.ATENDIDO : StatusFila.CANCELADO);
            fila.setPrioridade(Prioridade.values()[random.nextInt(Prioridade.values().length)]);
            fila.setMedico(sortear(medicos));
            fila.setPaciente(sortear(pacientes));
            em.persist(fila);
        }
    }

    private LocalDateTime horarioPassado(LocalDateTime referencia) {
        LocalDateTime inicio = referencia.minusYears(2);
        long segundos = Duration.between(inicio, referencia.minusHours(1)).getSeconds();
        return inicio.plusSeconds((long) (random.nextDouble() * segundos));
    }

    private Endereco endereco() {
        return new Endereco(limitar(faker.address().streetAddress(), 45),
                limitar("Bairro " + faker.name().lastName(), 45),
                limitar(faker.address().city(), 45), faker.address().stateAbbr());
    }

    private String telefone() {
        return limitar(faker.phoneNumber().cellPhone().replaceAll("\\D", ""), 20);
    }

    private String limitar(String valor, int tamanho) {
        return valor.substring(0, Math.min(valor.length(), tamanho));
    }

    private <T> T sortear(List<T> lista) {
        return lista.get(random.nextInt(lista.size()));
    }

    private String cpfLivre(Set<String> existentes) {
        String cpf;
        do {
            cpf = faker.cpf().valid(false);
        } while (!existentes.add(cpf));
        return cpf;
    }
}
