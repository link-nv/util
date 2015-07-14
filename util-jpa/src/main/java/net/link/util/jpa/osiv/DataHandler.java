package net.link.util.jpa.osiv;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import net.link.util.logging.Logger;


/**
 * User: gvhoecke <gianni.vanhoecke@lin-k.net>
 * Date: 02/09/13
 * Time: 13:39
 */
public class DataHandler {

    private static final Logger logger = Logger.get( DataHandler.class );

    private static final ThreadLocal<EntityManager> entityManagers = new ThreadLocal<EntityManager>();
    private static EntityManagerFactory emf;

    /**
     * Initialize the @{EntityManagerFactory} for specified persistence unit.
     */
    public static void initEntityManagerFactory(final String persistenceUnitName) {

        try {

            emf = Persistence.createEntityManagerFactory( persistenceUnitName );
        }
        catch (Throwable ex) {

            logger.err( ex, "Initial SessionFactory creation failed" );
            throw new ExceptionInInitializerError( ex );
        }

    }

    public static EntityManager em() {

        if (entityManagers.get() == null) {

            EntityManager em = emf.createEntityManager();
            entityManagers.set( em );
            em.getTransaction().begin();
        }

        return entityManagers.get();
    }

    public static void commit() {

        EntityManager em = entityManagers.get();

        if (em != null) {

            if (em.getTransaction().isActive()) {

                em.getTransaction().commit();
            }

            if (em.isOpen()) {

                em.close();
            }
        }

        entityManagers.set( null );
    }

    public static void rollback() {

        EntityManager em = entityManagers.get();

        if (em != null) {

            if (em.getTransaction().isActive()) {

                em.getTransaction().rollback();
            }
            if (em.isOpen()) {

                em.close();
            }
        }

        entityManagers.set( null );
    }

    public static Query createNamedQuery(final String queryName) {

        return em().createNamedQuery( queryName );
    }

}
