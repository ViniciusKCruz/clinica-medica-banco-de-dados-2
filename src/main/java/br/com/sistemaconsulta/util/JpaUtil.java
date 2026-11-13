package br.com.sistemaconsulta.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/** Mantem uma fabrica independente para cada unidade de persistencia. */
public final class JpaUtil {
    private static final String DEFAULT_PU = "sistemaConsultaPU";
    private static final Map<String, EntityManagerFactory> FACTORIES = new HashMap<>();

    private JpaUtil() {
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        return getEntityManagerFactory(DEFAULT_PU);
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory(String persistenceUnit) {
        EntityManagerFactory factory = FACTORIES.get(persistenceUnit);
        if (factory == null || !factory.isOpen()) {
            factory = DEFAULT_PU.equals(persistenceUnit)
                    ? Persistence.createEntityManagerFactory(persistenceUnit, DatabaseConfig.load())
                    : Persistence.createEntityManagerFactory(persistenceUnit);
            FACTORIES.put(persistenceUnit, factory);
        }
        return factory;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static EntityManager getEntityManager(String persistenceUnit) {
        return getEntityManagerFactory(persistenceUnit).createEntityManager();
    }

    public static synchronized void close() {
        try {
            for (EntityManagerFactory factory : FACTORIES.values()) {
                if (factory.isOpen()) {
                    factory.close();
                }
            }
        } finally {
            FACTORIES.clear();
        }
    }
}
