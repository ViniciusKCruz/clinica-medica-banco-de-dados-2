package br.com.sistemaconsulta.dao;

import br.com.sistemaconsulta.model.Paciente;
import javax.persistence.EntityManagerFactory;

public class PacienteDAO extends GenericDAO<Paciente, Long> {
    public PacienteDAO() {
        super(Paciente.class);
    }

    public PacienteDAO(EntityManagerFactory factory) {
        super(Paciente.class, factory);
    }
}
