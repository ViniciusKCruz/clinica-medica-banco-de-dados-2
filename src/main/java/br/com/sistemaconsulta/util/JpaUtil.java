package br.com.sistemaconsulta.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Fábrica simples de {@link EntityManager} para a Etapa 1 e etapas seguintes.
 */
public final class JpaUtil {

    private static final String DEFAULT_PU = "sistemaConsultaPU";
    private static EntityManagerFactory emf;

    private JpaUtil() {
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        return getEntityManagerFactory(DEFAULT_PU);
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory(String persistenceUnit) {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory(persistenceUnit);
        }
        return emf;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static EntityManager getEntityManager(String persistenceUnit) {
        return getEntityManagerFactory(persistenceUnit).createEntityManager();
    }

    public static synchronized void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }
}
