package br.com.sistemaconsulta.dao;

import br.com.sistemaconsulta.model.MedicoEspecialidade;
import javax.persistence.EntityManagerFactory;

public class MedicoEspecialidadeDAO extends GenericDAO<MedicoEspecialidade, Long> {
    public MedicoEspecialidadeDAO() {
        super(MedicoEspecialidade.class);
    }

    public MedicoEspecialidadeDAO(EntityManagerFactory factory) {
        super(MedicoEspecialidade.class, factory);
    }
}
