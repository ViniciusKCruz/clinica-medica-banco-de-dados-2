package br.com.sistemaconsulta.dao;

import br.com.sistemaconsulta.util.JpaUtil;
import br.com.sistemaconsulta.dao.dto.Pagina;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
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
        return executar(em -> criarConsultaListagem(em).getResultList());
    }

    public List<T> buscarPagina(int pagina, int tamanho) {
        validarPaginacao(pagina, tamanho);
        return executar(em -> criarConsultaPagina(em)
                .setFirstResult(pagina * tamanho).setMaxResults(tamanho).getResultList());
    }

    public long contar() {
        return executar(em -> criarConsultaContagem(em).getSingleResult());
    }

    public Pagina<T> consultarPagina(int pagina, int tamanho) {
        validarPaginacao(pagina, tamanho);
        return executar(em -> paginar(criarConsultaPagina(em), criarConsultaContagem(em), pagina, tamanho));
    }

    protected TypedQuery<T> criarConsultaListagem(EntityManager em) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> consulta = builder.createQuery(entidade);
        Root<T> registro = consulta.from(entidade);
        consulta.select(registro).orderBy(builder.asc(registro.get("id")));
        return em.createQuery(consulta);
    }

    protected TypedQuery<T> criarConsultaPagina(EntityManager em) {
        return criarConsultaListagem(em);
    }

    private TypedQuery<Long> criarConsultaContagem(EntityManager em) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Long> consulta = builder.createQuery(Long.class);
        consulta.select(builder.count(consulta.from(entidade)));
        return em.createQuery(consulta);
    }

    protected final <R> R primeiroOuNulo(TypedQuery<R> consulta) {
        List<R> resultados = consulta.setMaxResults(1).getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    protected final <R> Pagina<R> paginar(TypedQuery<R> consulta, TypedQuery<Long> contagem,
                                         int pagina, int tamanho) {
        validarPaginacao(pagina, tamanho);
        List<R> registros = consulta.setFirstResult(pagina * tamanho).setMaxResults(tamanho).getResultList();
        return new Pagina<>(registros, contagem.getSingleResult(), tamanho);
    }

    protected static void validarPaginacao(int pagina, int tamanho) {
        if (pagina < 0 || tamanho <= 0 || (long) pagina * tamanho > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Informe pagina nao negativa e tamanho positivo, dentro do limite de paginacao.");
        }
    }

    protected final <R> R executar(Function<EntityManager, R> operacao) {
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
