package br.com.sistemaconsulta.dao;

import br.com.sistemaconsulta.dao.dto.TotalConsultasPorMedico;
import br.com.sistemaconsulta.dao.dto.TotalConsultasPorStatus;
import br.com.sistemaconsulta.model.Consulta;
import br.com.sistemaconsulta.model.enums.StatusConsulta;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConsultaDAO extends GenericDAO<Consulta, Long> {
    public ConsultaDAO() {
        super(Consulta.class);
    }

    public ConsultaDAO(EntityManagerFactory factory) {
        super(Consulta.class, factory);
    }

    public List<Consulta> buscarPorStatus(StatusConsulta status) {
        Objects.requireNonNull(status, "Informe o status da consulta.");
        return executar(em -> em.createNamedQuery("Consulta.porStatus", Consulta.class)
                .setParameter("status", status).getResultList());
    }

    public List<Consulta> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        Objects.requireNonNull(inicio, "Informe o inicio do periodo.");
        Objects.requireNonNull(fim, "Informe o fim do periodo.");
        validarPeriodo(inicio, fim);
        return executar(em -> em.createQuery("SELECT c FROM Consulta c "
                        + "WHERE c.horario >= :inicio AND c.horario < :fim ORDER BY c.horario, c.id", Consulta.class)
                .setParameter("inicio", inicio).setParameter("fim", fim).getResultList());
    }

    public List<Consulta> buscarPorMedico(Long medicoId) {
        validarId(medicoId);
        return executar(em -> em.createQuery("SELECT c FROM Consulta c JOIN c.medico m "
                        + "WHERE m.id = :medicoId ORDER BY c.horario, c.id", Consulta.class)
                .setParameter("medicoId", medicoId).getResultList());
    }

    public Consulta buscarDetalhadaPorId(Long id) {
        validarId(id);
        return executar(em -> {
            List<Consulta> resultados = em.createQuery("SELECT c FROM Consulta c "
                            + "LEFT JOIN FETCH c.paciente LEFT JOIN FETCH c.medico "
                            + "LEFT JOIN FETCH c.prontuario WHERE c.id = :id", Consulta.class)
                    .setParameter("id", id).getResultList();
            return resultados.isEmpty() ? null : resultados.get(0);
        });
    }

    public List<TotalConsultasPorStatus> contarPorStatus() {
        return executar(em -> em.createQuery("SELECT NEW br.com.sistemaconsulta.dao.dto.TotalConsultasPorStatus("
                        + "c.statusConsulta, COUNT(c)) FROM Consulta c "
                        + "GROUP BY c.statusConsulta ORDER BY c.statusConsulta", TotalConsultasPorStatus.class)
                .getResultList());
    }

    public List<TotalConsultasPorMedico> contarPorMedico(long minimoConsultas) {
        if (minimoConsultas < 0) {
            throw new IllegalArgumentException("O minimo de consultas nao pode ser negativo.");
        }
        return executar(em -> em.createQuery("SELECT NEW br.com.sistemaconsulta.dao.dto.TotalConsultasPorMedico("
                        + "m.id, m.nome, COUNT(c)) FROM Medico m LEFT JOIN m.consultas c "
                        + "GROUP BY m.id, m.nome HAVING COUNT(c) >= :minimo ORDER BY COUNT(c) DESC, m.id",
                        TotalConsultasPorMedico.class)
                .setParameter("minimo", minimoConsultas).getResultList());
    }

    public List<Consulta> buscarPagina(int pagina, int tamanho) {
        if (pagina < 0 || tamanho <= 0 || (long) pagina * tamanho > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Informe pagina nao negativa e tamanho positivo, dentro do limite de paginacao.");
        }
        return executar(em -> em.createQuery("SELECT c FROM Consulta c ORDER BY c.horario, c.id", Consulta.class)
                .setFirstResult(pagina * tamanho).setMaxResults(tamanho).getResultList());
    }

    public List<Consulta> buscarPorStatusCriteria(StatusConsulta status) {
        Objects.requireNonNull(status, "Informe o status da consulta.");
        return executar(em -> {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<Consulta> query = builder.createQuery(Consulta.class);
            Root<Consulta> consulta = query.from(Consulta.class);
            query.select(consulta).where(builder.equal(consulta.get("statusConsulta"), status))
                    .orderBy(builder.asc(consulta.get("horario")), builder.asc(consulta.get("id")));
            return em.createQuery(query).getResultList();
        });
    }

    public List<Consulta> buscarComFiltros(Long medicoId, Long pacienteId, StatusConsulta status,
                                         LocalDateTime inicio, LocalDateTime fim) {
        if (medicoId != null) {
            validarId(medicoId);
        }
        if (pacienteId != null) {
            validarId(pacienteId);
        }
        validarPeriodo(inicio, fim);
        return executar(em -> {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<Consulta> query = builder.createQuery(Consulta.class);
            Root<Consulta> consulta = query.from(Consulta.class);
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
            query.select(consulta).where(filtros.toArray(new Predicate[0]))
                    .orderBy(builder.asc(consulta.get("horario")), builder.asc(consulta.get("id")));
            return em.createQuery(query).getResultList();
        });
    }

    private static void validarPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio != null && fim != null && !inicio.isBefore(fim)) {
            throw new IllegalArgumentException("O inicio do periodo deve ser anterior ao fim.");
        }
    }

    private static void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Informe um ID positivo.");
        }
    }
}
