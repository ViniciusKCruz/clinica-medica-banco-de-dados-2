package br.com.sistemaconsulta.dao;

import br.com.sistemaconsulta.model.Paciente;
import br.com.sistemaconsulta.model.Convenio;
import br.com.sistemaconsulta.dao.dto.Pagina;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PacienteDAO extends GenericDAO<Paciente, Long> {
    private static final String SELECAO = "SELECT p FROM Paciente p LEFT JOIN FETCH p.convenio";
    public PacienteDAO() {
        super(Paciente.class);
    }

    public PacienteDAO(EntityManagerFactory factory) {
        super(Paciente.class, factory);
    }

    @Override
    public Paciente buscarPorId(Long id) {
        Objects.requireNonNull(id);
        return executar(em -> primeiroOuNulo(em.createQuery(SELECAO + " WHERE p.id = :id", Paciente.class)
                .setParameter("id", id)));
    }

    @Override
    protected TypedQuery<Paciente> criarConsultaListagem(EntityManager em) {
        return em.createQuery(SELECAO + " ORDER BY p.id", Paciente.class);
    }

    public Convenio buscarConvenio(Long pacienteId) {
        Objects.requireNonNull(pacienteId);
        return executar(em -> primeiroOuNulo(em.createQuery("SELECT c FROM Paciente p JOIN p.convenio c "
                        + "WHERE p.id = :pacienteId", Convenio.class).setParameter("pacienteId", pacienteId)));
    }

    public Paciente salvarComConvenio(Paciente paciente, Long convenioId) {
        Objects.requireNonNull(paciente);
        return executar(em -> {
            paciente.setConvenio(localizarConvenio(em, convenioId));
            em.persist(paciente);
            return paciente;
        });
    }

    public Paciente atualizarComConvenio(Paciente paciente, Long convenioId) {
        Objects.requireNonNull(paciente);
        return executar(em -> {
            Paciente atualizado = em.merge(paciente);
            atualizado.setConvenio(localizarConvenio(em, convenioId));
            return atualizado;
        });
    }

    private Convenio localizarConvenio(EntityManager em, Long convenioId) {
        if (convenioId == null) {
            return null;
        }
        Convenio convenio = em.find(Convenio.class, convenioId);
        if (convenio == null) {
            throw new IllegalArgumentException("Convenio nao encontrado.");
        }
        return convenio;
    }

    public List<Paciente> buscarPorConvenio(Long convenioId) {
        validarConvenioId(convenioId);
        return executar(em -> consultaPorConvenio(em, convenioId).getResultList());
    }

    public Pagina<Paciente> buscarPorConvenio(Long convenioId, int pagina, int tamanho) {
        validarConvenioId(convenioId);
        validarPaginacao(pagina, tamanho);
        return executar(em -> paginar(
                consultaPorConvenio(em, convenioId),
                em.createQuery("SELECT COUNT(p) FROM Paciente p WHERE p.convenio.id = :convenioId", Long.class)
                        .setParameter("convenioId", convenioId), pagina, tamanho));
    }

    private TypedQuery<Paciente> consultaPorConvenio(EntityManager em, Long convenioId) {
        return em.createNamedQuery("Paciente.porConvenio", Paciente.class).setParameter("convenioId", convenioId);
    }

    public List<Paciente> buscarPorNomeCriteria(String trecho) {
        String padrao = padraoNome(trecho);
        return executar(em -> consultaPorNome(em, padrao).getResultList());
    }

    public Pagina<Paciente> buscarPorNomeCriteria(String trecho, int pagina, int tamanho) {
        String padrao = padraoNome(trecho);
        validarPaginacao(pagina, tamanho);
        return executar(em -> {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<Long> contagem = builder.createQuery(Long.class);
            Root<Paciente> paciente = contagem.from(Paciente.class);
            contagem.select(builder.count(paciente))
                    .where(builder.like(builder.lower(paciente.get("nome")), padrao, '\\'));
            return paginar(consultaPorNome(em, padrao), em.createQuery(contagem), pagina, tamanho);
        });
    }

    private TypedQuery<Paciente> consultaPorNome(EntityManager em, String padrao) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Paciente> query = builder.createQuery(Paciente.class);
        Root<Paciente> paciente = query.from(Paciente.class);
        paciente.fetch("convenio", JoinType.LEFT);
        query.select(paciente).where(builder.like(builder.lower(paciente.get("nome")), padrao, '\\'))
                .orderBy(builder.asc(paciente.get("nome")), builder.asc(paciente.get("id")));
        return em.createQuery(query);
    }

    private static String padraoNome(String trecho) {
        Objects.requireNonNull(trecho, "Informe parte do nome do paciente.");
        if (trecho.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe parte do nome do paciente.");
        }
        return "%" + trecho.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }

    private static void validarConvenioId(Long convenioId) {
        if (convenioId == null || convenioId <= 0) {
            throw new IllegalArgumentException("Informe um ID de convenio positivo.");
        }
    }
}
