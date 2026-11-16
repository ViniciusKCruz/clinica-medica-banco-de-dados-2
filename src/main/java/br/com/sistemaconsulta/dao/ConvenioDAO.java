package br.com.sistemaconsulta.dao;

import br.com.sistemaconsulta.model.Convenio;
import javax.persistence.EntityManagerFactory;

public class ConvenioDAO extends GenericDAO<Convenio, Long> {
    public ConvenioDAO() {
        super(Convenio.class);
    }

    public ConvenioDAO(EntityManagerFactory factory) {
        super(Convenio.class, factory);
    }
}
