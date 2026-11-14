package br.com.sistemaconsulta.dao;

import br.com.sistemaconsulta.model.Consulta;
import javax.persistence.EntityManagerFactory;

public class ConsultaDAO extends GenericDAO<Consulta, Long> {
    public ConsultaDAO() {
        super(Consulta.class);
    }

    public ConsultaDAO(EntityManagerFactory factory) {
        super(Consulta.class, factory);
    }
}
