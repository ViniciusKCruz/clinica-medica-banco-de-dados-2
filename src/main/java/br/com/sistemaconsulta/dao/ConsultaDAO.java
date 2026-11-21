package br.com.sistemaconsulta.dao;

import br.com.sistemaconsulta.dao.dto.TotalConsultasPorMedico;
import br.com.sistemaconsulta.dao.dto.TotalConsultasPorStatus;
import br.com.sistemaconsulta.dao.dto.Pagina;
import br.com.sistemaconsulta.model.Consulta;
import br.com.sistemaconsulta.model.enums.StatusConsulta;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConsultaDAO extends GenericDAO<Consulta, Long> {
    private static final String SELECAO = "SELECT c FROM Consulta c LEFT JOIN FETCH c.paciente "
            + "LEFT JOIN FETCH c.medico LEFT JOIN FETCH c.prontuario";
    private static final String ORDEM = " ORDER BY c.horario, c.id";
    private static final String FILTRO_PERIODO = " WHERE c.horario >= :inicio AND c.horario < :fim";
    private static final String FILTRO_MEDICO = " WHERE c.medico.id = :medicoId";

    public ConsultaDAO() {
        super(Consulta.class);
    }

    public ConsultaDAO(EntityManagerFactory factory) {
        super(Consulta.class, factory);
    }

    public List<Consulta> buscarPorStatus(StatusConsulta status) {
        Objects.requireNonNull(status, "Informe o status da consulta.");
        return executar(em -> consultaPorStatus(em, status).getResultList());
    }

    public Pagina<Consulta> buscarPorStatus(StatusConsulta status, int pagina, int tamanho) {
        Objects.requireNonNull(status, "Informe o status da consulta.");
        validarPaginacao(pagina, tamanho);
        return executar(em -> paginar(
                consultaPorStatus(em, status),
                em.createQuery("SELECT COUNT(c) FROM Consulta c WHERE c.statusConsulta = :status", Long.class)
                        .setParameter("status", status), pagina, tamanho));
    }

    public List<Consulta> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        validarPeriodoObrigatorio(inicio, fim);
        return executar(em -> consultaPorPeriodo(em, inicio, fim).getResultList());
    }

    public Pagina<Consulta> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim, int pagina, int tamanho) {
        validarPeriodoObrigatorio(inicio, fim);
        validarPaginacao(pagina, tamanho);
        return executar(em -> paginar(
                consultaPorPeriodo(em, inicio, fim),
                em.createQuery("SELECT COUNT(c) FROM Consulta c" + FILTRO_PERIODO, Long.class)
                        .setParameter("inicio", inicio).setParameter("fim", fim), pagina, tamanho));
    }

    public List<Consulta> buscarPorMedico(Long medicoId) {
        validarId(medicoId);
        return executar(em -> consultaPorMedico(em, medicoId).getResultList());
    }

    public Pagina<Consulta> buscarPorMedico(Long medicoId, int pagina, int tamanho) {
        validarId(medicoId);
        validarPaginacao(pagina, tamanho);
        return executar(em -> paginar(
                consultaPorMedico(em, medicoId),
                em.createQuery("SELECT COUNT(c) FROM Consulta c" + FILTRO_MEDICO, Long.class)
                        .setParameter("medicoId", medicoId), pagina, tamanho));
    }

    public Consulta buscarDetalhadaPorId(Long id) {
        validarId(id);
        return executar(em -> primeiroOuNulo(em.createQuery(SELECAO + " WHERE c.id = :id", Consulta.class)
                .setParameter("id", id)));
    }

    public List<TotalConsultasPorStatus> contarPorStatus() {
        return executar(em -> em.createQuery("SELECT NEW br.com.sistemaconsulta.dao.dto.TotalConsultasPorStatus("
                        + "c.statusConsulta, COUNT(c)) FROM Consulta c "
                        + "GROUP BY c.statusConsulta ORDER BY c.statusConsulta", TotalConsultasPorStatus.class)
                .getResultList());
    }

    public List<TotalConsultasPorMedico> contarPorMedico(long minimoConsultas) {
        validarMinimoConsultas(minimoConsultas);
        return executar(em -> consultaTotalPorMedico(em, minimoConsultas).getResultList());
    }

    public Pagina<TotalConsultasPorMedico> contarPorMedico(long minimoConsultas, int pagina, int tamanho) {
        validarMinimoConsultas(minimoConsultas);
        validarPaginacao(pagina, tamanho);
        return executar(em -> paginar(
                consultaTotalPorMedico(em, minimoConsultas),
                em.createQuery("SELECT COUNT(m) FROM Medico m WHERE "
                                + "(SELECT COUNT(c) FROM Consulta c WHERE c.medico = m) >= :minimo", Long.class)
                        .setParameter("minimo", minimoConsultas), pagina, tamanho));
    }

    @Override
    protected TypedQuery<Consulta> criarConsultaPagina(EntityManager em) {
        return em.createQuery(SELECAO + ORDEM, Consulta.class);
    }

    public List<Consulta> buscarPorStatusCriteria(StatusConsulta status) {
        Objects.requireNonNull(status, "Informe o status da consulta.");
        return buscarComFiltros(null, null, status, null, null);
    }

    public Pagina<Consulta> buscarPorStatusCriteria(StatusConsulta status, int pagina, int tamanho) {
        Objects.requireNonNull(status, "Informe o status da consulta.");
        return buscarComFiltros(null, null, status, null, null, pagina, tamanho);
    }

    public List<Consulta> buscarComFiltros(Long medicoId, Long pacienteId, StatusConsulta status,
                                         LocalDateTime inicio, LocalDateTime fim) {
        validarFiltros(medicoId, pacienteId, inicio, fim);
        return executar(em -> consultaComFiltros(em, medicoId, pacienteId, status, inicio, fim).getResultList());
    }

    public Pagina<Consulta> buscarComFiltros(Long medicoId, Long pacienteId, StatusConsulta status,
                                            LocalDateTime inicio, LocalDateTime fim, int pagina, int tamanho) {
        validarFiltros(medicoId, pacienteId, inicio, fim);
        validarPaginacao(pagina, tamanho);
        return executar(em -> {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<Long> contagem = builder.createQuery(Long.class);
            Root<Consulta> consulta = contagem.from(Consulta.class);
            contagem.select(builder.count(consulta))
                    .where(montarFiltros(builder, consulta, medicoId, pacienteId, status, inicio, fim));
            return paginar(consultaComFiltros(em, medicoId, pacienteId, status, inicio, fim),
                    em.createQuery(contagem), pagina, tamanho);
        });
    }

    private static void validarFiltros(Long medicoId, Long pacienteId, LocalDateTime inicio, LocalDateTime fim) {
        if (medicoId != null) {
            validarId(medicoId);
        }
        if (pacienteId != null) {
            validarId(pacienteId);
        }
        validarPeriodo(inicio, fim);
    }

    private TypedQuery<Consulta> consultaPorStatus(EntityManager em, StatusConsulta status) {
        return em.createNamedQuery("Consulta.porStatus", Consulta.class).setParameter("status", status);
    }

    private TypedQuery<Consulta> consultaPorPeriodo(EntityManager em, LocalDateTime inicio, LocalDateTime fim) {
        return em.createQuery(SELECAO + FILTRO_PERIODO + ORDEM, Consulta.class)
                .setParameter("inicio", inicio).setParameter("fim", fim);
    }

    private TypedQuery<Consulta> consultaPorMedico(EntityManager em, Long medicoId) {
        return em.createQuery(SELECAO + FILTRO_MEDICO + ORDEM, Consulta.class).setParameter("medicoId", medicoId);
    }

    private TypedQuery<TotalConsultasPorMedico> consultaTotalPorMedico(EntityManager em, long minimoConsultas) {
        return em.createQuery("SELECT NEW br.com.sistemaconsulta.dao.dto.TotalConsultasPorMedico("
                        + "m.id, m.nome, COUNT(c)) FROM Medico m LEFT JOIN m.consultas c "
                        + "GROUP BY m.id, m.nome HAVING COUNT(c) >= :minimo ORDER BY COUNT(c) DESC, m.id",
                        TotalConsultasPorMedico.class).setParameter("minimo", minimoConsultas);
    }

    private TypedQuery<Consulta> consultaComFiltros(EntityManager em, Long medicoId, Long pacienteId,
                                                   StatusConsulta status, LocalDateTime inicio, LocalDateTime fim) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Consulta> query = builder.createQuery(Consulta.class);
        Root<Consulta> consulta = query.from(Consulta.class);
        consulta.fetch("paciente", JoinType.LEFT);
        consulta.fetch("medico", JoinType.LEFT);
        consulta.fetch("prontuario", JoinType.LEFT);
        query.select(consulta).where(montarFiltros(builder, consulta, medicoId, pacienteId, status, inicio, fim))
                .orderBy(builder.asc(consulta.get("horario")), builder.asc(consulta.get("id")));
        return em.createQuery(query);
    }

    private static Predicate[] montarFiltros(CriteriaBuilder builder, Root<Consulta> consulta,
                                             Long medicoId, Long pacienteId, StatusConsulta status,
                                             LocalDateTime inicio, LocalDateTime fim) {
        List<Predicate> filtros = new ArrayList<>();
        if (medicoId != null) {
            filtros.add(builder.equal(consulta.get("medico").get("id"), medicoId));
        }
        if (pacienteId != null) {
            filtros.add(builder.equal(consulta.get("paciente").get("id"), pacienteId));
        }
        if (status != null) {
            filtros.add(builder.equal(consulta.get("statusConsulta"), status));
        }
        if (inicio != null) {
            filtros.add(builder.greaterThanOrEqualTo(consulta.get("horario"), inicio));
        }
        if (fim != null) {
            filtros.add(builder.lessThan(consulta.get("horario"), fim));
        }
        return filtros.toArray(new Predicate[0]);
    }

    private static void validarPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio != null && fim != null && !inicio.isBefore(fim)) {
            throw new IllegalArgumentException("O inicio do periodo deve ser anterior ao fim.");
        }
    }

    private static void validarPeriodoObrigatorio(LocalDateTime inicio, LocalDateTime fim) {
        Objects.requireNonNull(inicio, "Informe o inicio do periodo.");
        Objects.requireNonNull(fim, "Informe o fim do periodo.");
        validarPeriodo(inicio, fim);
    }

    private static void validarMinimoConsultas(long minimoConsultas) {
        if (minimoConsultas < 0) {
            throw new IllegalArgumentException("O minimo de consultas nao pode ser negativo.");
        }
    }

    private static void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Informe um ID positivo.");
        }
    }
}
