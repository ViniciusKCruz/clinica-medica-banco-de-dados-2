package br.com.sistemaconsulta.dao;

import br.com.sistemaconsulta.model.Paciente;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PacienteDAO extends GenericDAO<Paciente, Long> {
    public PacienteDAO() {
        super(Paciente.class);
    }

    public PacienteDAO(EntityManagerFactory factory) {
        super(Paciente.class, factory);
    }

    public List<Paciente> buscarPorConvenio(Long convenioId) {
        if (convenioId == null || convenioId <= 0) {
            throw new IllegalArgumentException("Informe um ID de convenio positivo.");
        }
        return executar(em -> em.createNamedQuery("Paciente.porConvenio", Paciente.class)
                .setParameter("convenioId", convenioId).getResultList());
    }

    public List<Paciente> buscarPorNomeCriteria(String trecho) {
        Objects.requireNonNull(trecho, "Informe parte do nome do paciente.");
        if (trecho.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe parte do nome do paciente.");
        }
        String padrao = "%" + trecho.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        return executar(em -> {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<Paciente> query = builder.createQuery(Paciente.class);
            Root<Paciente> paciente = query.from(Paciente.class);
            query.select(paciente).where(builder.like(builder.lower(paciente.get("nome")), padrao, '\\'))
                    .orderBy(builder.asc(paciente.get("nome")), builder.asc(paciente.get("id")));
            return em.createQuery(query).getResultList();
        });
    }
}
