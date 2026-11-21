package br.com.sistemaconsulta.dao;

import br.com.sistemaconsulta.dao.dto.ResumoSalariosPorStatus;
import br.com.sistemaconsulta.dao.dto.Pagina;
import br.com.sistemaconsulta.model.Medico;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.util.List;

public class MedicoDAO extends GenericDAO<Medico, Long> {
    private static final String FILTRO_SALARIO = " WHERE m.salario > (SELECT AVG(outro.salario) FROM Medico outro)";

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
        return executar(em -> consultaSalarioAcimaDaMedia(em).getResultList());
    }

    public Pagina<Medico> buscarComSalarioAcimaDaMedia(int pagina, int tamanho) {
        validarPaginacao(pagina, tamanho);
        return executar(em -> paginar(
                consultaSalarioAcimaDaMedia(em),
                em.createQuery("SELECT COUNT(m) FROM Medico m" + FILTRO_SALARIO, Long.class), pagina, tamanho));
    }

    private TypedQuery<Medico> consultaSalarioAcimaDaMedia(EntityManager em) {
        return em.createQuery("SELECT m FROM Medico m" + FILTRO_SALARIO + " ORDER BY m.salario DESC, m.id", Medico.class);
    }
}
