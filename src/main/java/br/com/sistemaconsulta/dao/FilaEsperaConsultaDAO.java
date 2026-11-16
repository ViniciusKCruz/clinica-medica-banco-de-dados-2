package br.com.sistemaconsulta.dao;

import br.com.sistemaconsulta.model.FilaEsperaConsulta;
import javax.persistence.EntityManagerFactory;

public class FilaEsperaConsultaDAO extends GenericDAO<FilaEsperaConsulta, Long> {
    public FilaEsperaConsultaDAO() {
        super(FilaEsperaConsulta.class);
    }

    public FilaEsperaConsultaDAO(EntityManagerFactory factory) {
        super(FilaEsperaConsulta.class, factory);
    }
}
