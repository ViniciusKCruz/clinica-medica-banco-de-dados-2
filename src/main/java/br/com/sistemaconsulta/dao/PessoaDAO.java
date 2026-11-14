package br.com.sistemaconsulta.dao;

import br.com.sistemaconsulta.model.Pessoa;
import javax.persistence.EntityManagerFactory;

public class PessoaDAO extends GenericDAO<Pessoa, Long> {
    public PessoaDAO() {
        super(Pessoa.class);
    }

    public PessoaDAO(EntityManagerFactory factory) {
        super(Pessoa.class, factory);
    }
}
