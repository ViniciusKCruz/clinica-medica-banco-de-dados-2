package br.com.sistemaconsulta.dao;

import br.com.sistemaconsulta.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class GenericDAO<T, ID> {
    private final Class<T> entidade;
    private final EntityManagerFactory factory;

    public GenericDAO(Class<T> entidade) {
        this(entidade, JpaUtil.getEntityManagerFactory());
    }

    public GenericDAO(Class<T> entidade, EntityManagerFactory factory) {
        this.entidade = Objects.requireNonNull(entidade);
        this.factory = Objects.requireNonNull(factory);
    }

    public T salvar(T objeto) {
        Objects.requireNonNull(objeto);
        return executar(em -> {
            em.persist(objeto);
            return objeto;
        });
    }

    public T atualizar(T objeto) {
        Objects.requireNonNull(objeto);
        return executar(em -> em.merge(objeto));
    }

    public boolean remover(ID id) {
        Objects.requireNonNull(id);
        return executar(em -> {
            T objeto = em.find(entidade, id);
            if (objeto == null) {
                return false;
            }
            em.remove(objeto);
            return true;
        });
    }

    public T buscarPorId(ID id) {
        Objects.requireNonNull(id);
        return executar(em -> em.find(entidade, id));
    }

    public List<T> buscarTodos() {
        return executar(em -> {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<T> consulta = builder.createQuery(entidade);
            Root<T> registro = consulta.from(entidade);
            consulta.select(registro).orderBy(builder.asc(registro.get("id")));
            return em.createQuery(consulta).getResultList();
        });
    }

    private <R> R executar(Function<EntityManager, R> operacao) {
        EntityManager em = factory.createEntityManager();
        EntityTransaction transacao = em.getTransaction();
        try {
            transacao.begin();
            R resultado = operacao.apply(em);
            transacao.commit();
            return resultado;
        } catch (RuntimeException e) {
            try {
                if (transacao.isActive()) {
                    transacao.rollback();
                }
            } catch (RuntimeException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
