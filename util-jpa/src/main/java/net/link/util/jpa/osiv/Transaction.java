package net.link.util.jpa.osiv;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import net.link.util.InternalInconsistencyException;
import net.link.util.logging.Logger;


public class Transaction {

    static final         Logger                     logger         = Logger.get( Transaction.class );
    private static final ThreadLocal<EntityManager> entityManagers = new ThreadLocal<EntityManager>();

    private static EntityManagerFactory emf;

    public static EntityManagerFactory getEntityManagerFactory() {

        return emf;
    }

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

    /**
     * Get current entity manager. If no transaction is active, start a new one.
     */
    public static EntityManager manager() {

        if (entityManagers.get() == null) {
            EntityManager em = getEntityManagerFactory().createEntityManager();
            entityManagers.set( em );
        }

        if (!entityManagers.get().getTransaction().isActive()) {
            entityManagers.get().getTransaction().begin();
        }

        return entityManagers.get();
    }

    public static void commit() {

        commit( true );
    }

    public static void finalCommit() {

        commit( false );
    }

    /**
     * Commit current transaction, optionally start a new one
     */
    public static void commit(final boolean startNewTransaction) {

        EntityManager em = entityManagers.get();
        if (em != null && em.isOpen()) {
            if (em.getTransaction().isActive() && !em.getTransaction().getRollbackOnly()) {
                em.getTransaction().commit();
                // start a new transaction
                if (startNewTransaction) {
                    em.getTransaction().begin();
                }
            } else if (em.getTransaction().isActive()) {
                logger.wrn( new IllegalStateException( "Cannot commit transaction" ),
                        "Cannot commit transaction: transaction is rollback-only, performing rollback instead" );
                em.getTransaction().rollback();
            } else {
                logger.wrn( new IllegalStateException( "Cannot commit transaction: transaction is not active" ),
                        "Cannot commit transaction: transaction is not active" );
            }
        } else if (em != null) {
            entityManagers.set( null ); // clear current em
            throw new InternalInconsistencyException( "Attempting to commit to a closed entity manager" );
        }
    }

    public static void rollback() {

        EntityManager em = entityManagers.get();
        if (em != null) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        }
    }

    public static void close() {

        EntityManager em = entityManagers.get();
        if (em != null) {
            // if there is still an active transaction, commit/rollback
            if (em.getTransaction().isActive()) {
                if (em.getTransaction().getRollbackOnly()) {
                    logger.wrn( "Rollback transaction before close" );
                    em.getTransaction().rollback();
                } else {
                    logger.wrn( "Commit your transaction before closing EntityManager" );
                    em.getTransaction().commit();
                }
            }
            if (em.isOpen()) {
                em.close();
            }
        }
        entityManagers.set( null );
    }
}