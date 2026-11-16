package br.com.sistemaconsulta.dao;

import br.com.sistemaconsulta.dao.dto.ResumoSalariosPorStatus;
import br.com.sistemaconsulta.model.Medico;
import javax.persistence.EntityManagerFactory;
import java.util.List;

public class MedicoDAO extends GenericDAO<Medico, Long> {
    public MedicoDAO() {
        super(Medico.class);
    }

    public MedicoDAO(EntityManagerFactory factory) {
        super(Medico.class, factory);
    }

    public List<ResumoSalariosPorStatus> resumirSalariosPorStatus() {
        return executar(em -> em.createQuery("SELECT NEW br.com.sistemaconsulta.dao.dto.ResumoSalariosPorStatus("
                        + "m.statusAtual, COUNT(m), SUM(m.salario), AVG(m.salario)) FROM Medico m "
                        + "GROUP BY m.statusAtual ORDER BY m.statusAtual", ResumoSalariosPorStatus.class)
                .getResultList());
    }

    public List<Medico> buscarComSalarioAcimaDaMedia() {
        return executar(em -> em.createQuery("SELECT m FROM Medico m "
                        + "WHERE m.salario > (SELECT AVG(outro.salario) FROM Medico outro) "
                        + "ORDER BY m.salario DESC, m.id", Medico.class)
                .getResultList());
    }
}
