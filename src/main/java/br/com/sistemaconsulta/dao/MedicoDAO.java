package br.com.sistemaconsulta.dao;

import br.com.sistemaconsulta.model.Medico;
import javax.persistence.EntityManagerFactory;

public class MedicoDAO extends GenericDAO<Medico, Long> {
    public MedicoDAO() {
        super(Medico.class);
    }

    public MedicoDAO(EntityManagerFactory factory) {
        super(Medico.class, factory);
    }
}
