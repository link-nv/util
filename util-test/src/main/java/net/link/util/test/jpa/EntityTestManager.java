/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.test.jpa;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import net.link.util.InternalInconsistencyException;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;


public class EntityTestManager {

    private int batchSize;

    private EntityManagerFactory entityManagerFactory;
    private EntityManager        entityManager;
    private Map<String, String>  configuration;

    public void configureHSql() {

        configuration = new HashMap<>();
        configuration.put( "hibernate.dialect", "org.hibernate.dialect.HSQLDialect" );
        configuration.put( "hibernate.show_sql", "true" );
        configuration.put( "hibernate.hbm2ddl.auto", "create-drop" );
        configuration.put( "hibernate.connection.username", "sa" );
        configuration.put( "hibernate.connection.password", "" );
        configuration.put( "hibernate.connection.driver_class", "org.hsqldb.jdbcDriver" );
        configuration.put( "hibernate.connection.url", "jdbc:hsqldb:mem:test" );
        // turn off batch processing, gives more informative errors that way
        configuration.put( "hibernate.jdbc.batch_size", String.valueOf( getBatchSize() ) );
    }

    @SuppressWarnings("unused")
    public void configureMySql(String host, int port, String database, String username, String password, boolean showSql) {

        configuration = new HashMap<>();
        configuration.put( "hibernate.dialect", "org.hibernate.dialect.MySQLDialect" );
        configuration.put( "hibernate.show_sql", Boolean.toString( showSql ) );
        configuration.put( "hibernate.hbm2ddl.auto", "validate" );
        configuration.put( "hibernate.connection.username", username );
        configuration.put( "hibernate.connection.password", password );
        configuration.put( "hibernate.connection.driver_class", "com.mysql.jdbc.Driver" );
        configuration.put( "hibernate.connection.url", String.format( "jdbc:mysql://%s:%d/%s", host, port, database ) );
        // turn off batch processing, gives more informative errors that way
        configuration.put( "hibernate.jdbc.batch_size", String.valueOf( getBatchSize() ) );
    }

    public void setUp(String persistenceUnitName)
            throws Exception {

        if (null == persistenceUnitName) {
            return;
        }

        if (configuration == null) {
            configureHSql();
        }
        
        entityManagerFactory = Persistence.createEntityManagerFactory( persistenceUnitName, configuration );
        /*
         * createEntityManagerFactory is deprecated, but buildEntityManagerFactory doesn't work because of a bug.
         */
        entityManager = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();
    }

    public int getBatchSize() {

        return batchSize;
    }

    @SuppressWarnings("unused")
    public void setBatchSize(int batchSize) {

        this.batchSize = batchSize;
    }

    public void tearDown()
            throws Exception {

        if (null != entityManagerFactory) {
            if (entityManager.isOpen()) {
                EntityTransaction entityTransaction = entityManager.getTransaction();
                if (entityTransaction.isActive())
                    if (entityTransaction.getRollbackOnly())
                        entityTransaction.rollback();
                    else
                        entityTransaction.commit();
                entityManager.close();
                entityManager = null;
            }
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }

    @SuppressWarnings("unused")
    public EntityManager refreshEntityManager() {

        if (entityManager.isOpen()) {
            EntityTransaction entityTransaction = entityManager.getTransaction();
            if (entityTransaction.isActive())
                if (entityTransaction.getRollbackOnly())
                    entityTransaction.rollback();
                else
                    entityTransaction.commit();
            entityManager.close();
        }
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        return entityManager;
    }

    @SuppressWarnings("unused")
    public void newTransaction() {

        EntityTransaction transaction = entityManager.getTransaction();
        transaction.commit();
        transaction.begin();
        entityManager.clear();
    }

    public EntityManager getEntityManager() {

        return entityManager;
    }

    /**
     * Create a new instance of the given class that has the test transaction entity manager handler applied to it. The transaction
     * semantics are:
     * <p/>
     * {@code @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)}
     */
    @SuppressWarnings({ "unchecked", "ClassNewInstance", "OverlyBroadCatchBlock" })
    public <T> T newInstance(Class<T> clazz) {

        T instance;
        try {
            instance = clazz.newInstance();
        }
        catch (InstantiationException ignored) {
            throw new InternalInconsistencyException( "instantiation error" );
        }
        catch (IllegalAccessException ignored) {
            throw new InternalInconsistencyException( "illegal access error" );
        }
        TransactionMethodInterceptor transactionInvocationHandler = new TransactionMethodInterceptor( instance, entityManagerFactory );
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass( clazz );
        enhancer.setCallback( transactionInvocationHandler );
        T object = (T) enhancer.create();
        try {
            init( clazz, object );
        }
        catch (Exception ignored) {
            throw new InternalInconsistencyException( "init error" );
        }
        return object;
    }

    public static void init(Class<?> clazz, Object bean)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            PostConstruct postConstruct = method.getAnnotation( PostConstruct.class );
            if (null == postConstruct)
                continue;
            method.invoke( bean );
        }
    }

    private static class TransactionMethodInterceptor implements MethodInterceptor {

        private final Object object;

        private final EntityManagerFactory entityManagerFactory;

        private final Field field;

        private TransactionMethodInterceptor(Object object, EntityManagerFactory entityManagerFactory) {

            this.object = object;
            this.entityManagerFactory = entityManagerFactory;
            field = getEntityManagerField( object );
        }

        private static Field getEntityManagerField(Object target) {

            Class<?> clazz = target.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field currentField : fields) {
                PersistenceContext persistenceContextAnnotation = currentField.getAnnotation( PersistenceContext.class );
                if (null == persistenceContextAnnotation)
                    continue;
                if (!EntityManager.class.isAssignableFrom( currentField.getType() ))
                    throw new InternalInconsistencyException( "field type not correct" );
                currentField.setAccessible( true );
                return currentField;
            }
            throw new InternalInconsistencyException( "no entity manager field found" );
        }

        @SuppressWarnings({ "OverlyBroadCatchBlock", "ProhibitedExceptionThrown", "CaughtExceptionImmediatelyRethrown" })
        @Override
        public Object intercept(@SuppressWarnings("unused") Object obj, Method method, Object[] args, @SuppressWarnings("unused") MethodProxy proxy)
                throws Throwable {

            EntityManager entityManager = entityManagerFactory.createEntityManager();
            try {
                field.set( object, entityManager );
                entityManager.getTransaction().begin();
                Object result = method.invoke( object, args );
                entityManager.getTransaction().commit();
                return result;
            }
            catch (InvocationTargetException e) {
                entityManager.getTransaction().rollback();
                throw e.getTargetException();
            }
            catch (Exception e) {
                throw e;
            }
            finally {
                entityManager.close();
            }
        }
    }
}
