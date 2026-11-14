package br.com.sistemaconsulta.dao;

import br.com.sistemaconsulta.model.Clinica;
import javax.persistence.EntityManagerFactory;

public class ClinicaDAO extends GenericDAO<Clinica, Long> {
    public ClinicaDAO() {
        super(Clinica.class);
    }

    public ClinicaDAO(EntityManagerFactory factory) {
        super(Clinica.class, factory);
    }
}
