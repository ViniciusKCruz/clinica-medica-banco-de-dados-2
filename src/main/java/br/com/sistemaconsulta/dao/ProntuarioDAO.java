package br.com.sistemaconsulta.dao;

import br.com.sistemaconsulta.model.Prontuario;
import javax.persistence.EntityManagerFactory;

public class ProntuarioDAO extends GenericDAO<Prontuario, Long> {
    public ProntuarioDAO() {
        super(Prontuario.class);
    }

    public ProntuarioDAO(EntityManagerFactory factory) {
        super(Prontuario.class, factory);
    }
}
