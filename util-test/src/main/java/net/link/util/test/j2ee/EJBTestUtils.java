/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.test.j2ee;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.*;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContextHandler;
import javax.transaction.*;
import javax.xml.rpc.handler.MessageContext;
import net.link.util.j2ee.EJBUtils;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;


/**
 * Util class for EJB3 unit testing.
 *
 * @author fcorneli
 */
public final class EJBTestUtils {

    static final Log LOG = LogFactory.getLog( EJBTestUtils.class );

    private EJBTestUtils() {

        // empty
    }

    /**
     * Injects a value into a given object's field of the given name.
     *
     * @param fieldName the name of the field to set.
     * @param object the object on which to inject the value.
     * @param value the value to inject.
     *
     * @throws Exception
     */
    public static void inject(String fieldName, Object object, Object value)
            throws Exception {

        if (null == fieldName)
            throw new IllegalArgumentException( "field name should not be null" );
        if (null == value)
            throw new IllegalArgumentException( "the value should not be null" );
        if (null == object)
            throw new IllegalArgumentException( "the object should not be null" );
        Field field = object.getClass().getDeclaredField( fieldName );
        field.setAccessible( true );
        field.set( object, value );
    }

    /**
     * Injects a resource value into an object's field which has the given resource name in a {@link Resource} annotation.
     *
     * @param bean the bean object on which to inject a value.
     * @param resourceName the source name.
     * @param value the value to inject.
     */
    public static void injectResource(Object bean, String resourceName, Object value)
            throws Exception {

        if (null == bean)
            throw new IllegalArgumentException( "the bean object should not be null" );
        if (null == resourceName)
            throw new IllegalArgumentException( "the resource name should not be null" );
        if (null == value)
            throw new IllegalArgumentException( "the value object should not be null" );
        Class<?> beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            Resource resourceAnnotation = field.getAnnotation( Resource.class );
            if (null == resourceAnnotation)
                continue;
            if (!resourceName.equals( resourceAnnotation.name() ))
                continue;
            field.setAccessible( true );
            field.set( bean, value );
            return;
        }
        throw new IllegalArgumentException( "resource field not found" );
    }

    /**
     * Injects the value into the object's field with a field type that matches the value.
     *
     * @param object the bean object in which to inject.
     * @param value the value object to inject.
     *
     * @throws Exception
     */
    public static void inject(Object object, Object value)
            throws Exception {

        if (null == value)
            throw new IllegalArgumentException( "the value should not be null" );
        if (null == object)
            throw new IllegalArgumentException( "the object should not be null" );

        Field selectedField = null;
        for (Class<?> type = object.getClass(); type != null; type = type.getSuperclass()) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields)
                if (field.getType().isInstance( value )) {
                    if (null != selectedField)
                        throw new IllegalStateException( "multiple fields of same injection type found" );
                    selectedField = field;
                }

            if (selectedField != null)
                break;
        }
        if (null == selectedField)
            throw new IllegalStateException( "field of injection type not found" );
        selectedField.setAccessible( true );
        selectedField.set( object, value );
        LOG.debug( "injected " + value + " into " + selectedField + " of " + object );
    }

    /**
     * Initializes a bean by invoking the {@link PostConstruct} annotated methods in it.
     *
     * @param bean the bean to initialize.
     *
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static void init(Object bean)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        Class<?> clazz = bean.getClass();
        init( clazz, bean );
    }

    /**
     * Initializes a bean by invoking the {@link PostConstruct} annotated methods in it.
     */
    public static void init(Class<?> clazz, Object bean)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        // LOG.debug("Initializing: " + bean);
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            PostConstruct postConstruct = method.getAnnotation( PostConstruct.class );
            if (null == postConstruct)
                continue;
            method.invoke( bean, new Object[] { } );
        }
    }

    public static void setJBossPrincipal(String principalName, String role) {

        Principal principal = new SimplePrincipal( principalName );
        SecurityAssociation.setPrincipal( principal );
        Subject subject = new Subject();
        subject.getPrincipals().add( principal );
        SimpleGroup rolesGroup = new SimpleGroup( "Roles" );
        rolesGroup.addMember( new SimplePrincipal( role ) );
        subject.getPrincipals().add( rolesGroup );
        SecurityAssociation.setSubject( subject );
    }

    /**
     * Instantiate a bean for the given type using a bean class from the given container; injecting the given entity manager and performing
     * {@link EJB} injections in it for the other beans provided by the container.
     */
    public static <Type> Type newInstance(Class<Type> clazz, Class<?>[] container, EntityManager entityManager) {

        return newInstance( clazz, container, entityManager, (String) null );
    }

    /**
     * Instantiate a bean for the given type using a bean class from the given container; injecting the given entity manager and performing
     * {@link EJB} injections in it for the other beans provided by the container.
     */
    public static <Type> Type newInstance(Class<Type> clazz, Class<?>[] container, EntityManager entityManager, String callerPrincipalName,
                                          String... roles) {

        TestSessionContext testSessionContext = new TestSessionContext( callerPrincipalName, roles );
        return newInstance( clazz, container, entityManager, testSessionContext );
    }

    /**
     * Instantiate a bean for the given type using a bean class from the given container; injecting the given entity manager and performing
     * {@link EJB} injections in it for the other beans provided by the container.
     */
    public static <Type> Type newInstance(Class<Type> clazz, Class<?>[] container, EntityManager entityManager,
                                          String callerPrincipalName) {

        TestSessionContext testSessionContext = new TestSessionContext( callerPrincipalName, (String[]) null );
        return newInstance( clazz, container, entityManager, testSessionContext );
    }

    /**
     * Instantiate a bean for the given type using a bean class from the given container; injecting the given entity manager and performing
     * {@link EJB} injections in it for the other beans provided by the container.
     */
    @SuppressWarnings("unchecked")
    public static <Type> Type newInstance(Class<Type> type, Class<?>[] container, EntityManager entityManager,
                                          SessionContext sessionContext) {

        Class<Type> beanType = type;
        if (type.isInterface()) {
            for (Class<?> beanClass : container)
                if (type.isAssignableFrom( beanClass )) {
                    beanType = (Class<Type>) beanClass;
                    break;
                }

            if (beanType.isInterface())
                throw new EJBException( "cannot instantiate interface: " + beanType + " (no instantiatable type found in container)" );
        }

        Type instance;
        try {
            instance = beanType.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException( "instantiation error: " + beanType );
        } catch (IllegalAccessException e) {
            throw new RuntimeException( "illegal access error: " + beanType );
        }
        TestContainerMethodInterceptor testContainerMethodInterceptor = new TestContainerMethodInterceptor( instance, container,
                entityManager, sessionContext );
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass( beanType );
        enhancer.setCallback( testContainerMethodInterceptor );
        try {
            Type object = (Type) enhancer.create();

            try {
                init( beanType, object );
                return object;
            } catch (Exception e) {
                throw new RuntimeException( "init error: " + beanType, e );
            }
        } catch (Exception e) {
            throw new RuntimeException( "CG Enhancer error: " + beanType, e );
        }
    }

    /**
     * Test EJB3 Container method interceptor. Be careful here not to start writing an entire EJB3 container.
     *
     * @author fcorneli
     */
    static class TestContainerMethodInterceptor implements MethodInterceptor {

        private static final Log interceptorLOG = LogFactory.getLog( TestContainerMethodInterceptor.class );

        private final Object object;

        private final Class<?>[] container;

        private final EntityManager entityManager;

        private final SessionContext sessionContext;

        public TestContainerMethodInterceptor(Object object, Class<?>[] container, EntityManager entityManager,
                                              SessionContext sessionContext) {

            this.object = object;
            this.container = container;
            this.entityManager = entityManager;
            this.sessionContext = sessionContext;
        }

        public Object intercept(@SuppressWarnings("unused") Object obj, Method method, Object[] args,
                                @SuppressWarnings("unused") MethodProxy proxy)
                throws Throwable {

            checkSessionBean();
            Class<?> clazz = object.getClass();
            checkSecurity( clazz, method );
            injectDependencies( clazz );
            injectEntityManager( clazz );
            injectResources( clazz );
            manageTransaction( method );
            try {
                method.setAccessible( true );
                Object result = method.invoke( object, args );
                return result;
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

        private void manageTransaction(Method method) {

            TransactionAttribute transactionAttributeAnnotation = method.getAnnotation( TransactionAttribute.class );
            if (null == transactionAttributeAnnotation)
                return;
            TransactionAttributeType transactionAttributeType = transactionAttributeAnnotation.value();
            switch (transactionAttributeType) {
                case REQUIRES_NEW:
                    if (entityManager != null) {
                        EntityTransaction entityTransaction = entityManager.getTransaction();
                        /*
                         * The following is not 100% correct, but will do for most of the tests.
                         */
                        interceptorLOG.debug( "transaction management: REQUIRED_NEW" );
                        entityTransaction.commit();
                        entityTransaction.begin();
                    }
                    break;
                default:
                    break;
            }
        }

        private void checkSecurity(Class<?> clazz, Method method) {

            SecurityDomain securityDomainAnnotation = clazz.getAnnotation( SecurityDomain.class );
            if (null == securityDomainAnnotation)
                return;
            // LOG.debug("security domain: " +
            // securityDomainAnnotation.value());
            Principal callerPrincipal = sessionContext.getCallerPrincipal();
            if (null == callerPrincipal)
                throw new EJBException( "caller principal should not be null" );
            // LOG.debug("caller principal: " + callerPrincipal.getName());
            RolesAllowed rolesAllowedAnnotation = method.getAnnotation( RolesAllowed.class );
            if (rolesAllowedAnnotation == null)
                return;
            String[] roles = rolesAllowedAnnotation.value();
            // LOG.debug("number of roles: " + roles.length);
            for (String role : roles)
                // LOG.debug("checking role: " + role);
                if (true == sessionContext.isCallerInRole( role ))
                    return;
            StringBuffer message = new StringBuffer();
            message.append( "user is not allowed to invoke the method. [allowed roles: " );
            for (String role : roles)
                message.append( "\"" + role + "\" " );
            message.append( "]" );
            throw new EJBException( message.toString() );
        }

        private void checkSessionBean() {

            Class<?> clazz = object.getClass();
            Stateless statelessAnnotation = clazz.getAnnotation( Stateless.class );
            Stateful statefulAnnotation = clazz.getAnnotation( Stateful.class );
            if (null == statelessAnnotation && statefulAnnotation == null)
                throw new EJBException( "no @Stateless nor @Stateful annotation found" );
        }

        private void injectResources(Class<?> clazz) {

            if (false == clazz.equals( Object.class ))
                injectResources( clazz.getSuperclass() );
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                Resource resourceAnnotation = field.getAnnotation( Resource.class );
                if (null == resourceAnnotation)
                    continue;
                Class<?> fieldType = field.getType();
                if (true == SessionContext.class.isAssignableFrom( fieldType )) {
                    setField( field, sessionContext );
                    continue;
                }
                if (true == TimerService.class.isAssignableFrom( fieldType )) {
                    TimerService testTimerService = new TestTimerService();
                    setField( field, testTimerService );
                    continue;
                }
                if (true == String.class.isAssignableFrom( fieldType ))
                    /*
                     * In this case we're probably dealing with an env-entry injection, which we can most of the time safely skip.
                     */
                    continue;
                if (true == ConnectionFactory.class.isAssignableFrom( fieldType ))
                    // JMS ConnectionFactory, we can probably safely skip.
                    continue;
                if (true == Queue.class.isAssignableFrom( fieldType ))
                    // JMS ConnectionFactory, we can probably safely skip.
                    continue;
                if (true == boolean.class.isAssignableFrom( fieldType ))
                    continue;
                if (true == UserTransaction.class.isAssignableFrom( fieldType )) {
                    setField( field, new UserTransaction() {

                        public void setTransactionTimeout(int arg0)
                                throws SystemException {

                        }

                        public void setRollbackOnly()
                                throws IllegalStateException, SystemException {

                        }

                        public void rollback()
                                throws IllegalStateException, SecurityException, SystemException {

                        }

                        public int getStatus()
                                throws SystemException {

                            return 0;
                        }

                        public void commit()
                                throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException,
                                       IllegalStateException, SystemException {

                        }

                        public void begin()
                                throws NotSupportedException, SystemException {

                        }
                    } );
                    continue;
                }

                throw new EJBException( "unsupported resource type: " + fieldType.getName() );
            }
        }

        @SuppressWarnings("unchecked")
        private void injectDependencies(Class<?> clazz) {

            if (false == clazz.equals( Object.class ))
                injectDependencies( clazz.getSuperclass() );
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                EJB ejbAnnotation = field.getAnnotation( EJB.class );
                if (null == ejbAnnotation)
                    continue;

                Class<?> fieldType = field.getType();
                if (field.getName().endsWith( "auditService" ))
                    LOG.debug( "injecting: " + fieldType + " into field: " + field + " of " + clazz );

                Object bean;
                try {
                    bean = EJBUtils.getEJB( fieldType );
                } catch (EJBException e) {
                    if (false == fieldType.isInterface())
                        throw new EJBException( "field is not an interface type" );
                    Local localAnnotation = fieldType.getAnnotation( Local.class );
                    if (null == localAnnotation)
                        throw new EJBException( "interface has no @Local annotation: " + fieldType.getName() );
                    Remote remoteAnnotation = fieldType.getAnnotation( Remote.class );
                    if (null != remoteAnnotation)
                        throw new EJBException( "interface cannot have both @Local and @Remote annotation" );

                    Class<?> beanType = getBeanType( fieldType, ejbAnnotation );
                    bean = EJBTestUtils.newInstance( beanType, container, entityManager, sessionContext );
                }

                setField( field, bean );
            }
        }

        private void setField(Field field, Object value) {

            try {
                field.setAccessible( true );
                if (field.get( object ) == null)
                    field.set( object, value );
            } catch (IllegalArgumentException e) {
                throw new EJBException( "illegal argument error" );
            } catch (IllegalAccessException e) {
                throw new EJBException( "illegal access error" );
            }
        }

        private void injectEntityManager(Class<?> clazz) {

            if (false == clazz.equals( Object.class ))
                injectEntityManager( clazz.getSuperclass() );
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                PersistenceContext persistenceContextAnnotation = field.getAnnotation( PersistenceContext.class );
                if (null == persistenceContextAnnotation)
                    continue;
                Class<?> fieldType = field.getType();
                if (false == EntityManager.class.isAssignableFrom( fieldType ))
                    throw new EJBException( "field type not correct" );
                setField( field, entityManager );
            }
        }

        private Class<?> getBeanType(Class<?> interfaceType, EJB ejbAnnotation) {

            for (Class<?> containerClass : container) {
                if (false == interfaceType.isAssignableFrom( containerClass ))
                    continue;
                LocalBinding localBinding = containerClass.getAnnotation( LocalBinding.class );
                if (null != localBinding)
                    if (localBinding.jndiBinding().equals( ejbAnnotation.mappedName() ))
                        return containerClass;
                RemoteBinding remoteBinding = containerClass.getAnnotation( RemoteBinding.class );
                if (null != remoteBinding)
                    if (remoteBinding.jndiBinding().equals( ejbAnnotation.mappedName() ))
                        return containerClass;
            }
            throw new EJBException( "did not find a container class for type: " + interfaceType.getName() );
        }
    }


    static class TestPolicyContextHandler implements PolicyContextHandler {

        private final Subject subject;

        public TestPolicyContextHandler(Principal principal, String... roles) {

            subject = new Subject();
            Set<Principal> principals = subject.getPrincipals();
            if (null != principal)
                principals.add( principal );
            if (null != roles) {
                SimpleGroup rolesGroup = new SimpleGroup( "Roles" );
                for (String role : roles)
                    rolesGroup.addMember( new SimplePrincipal( role ) );
                principals.add( rolesGroup );
            }
        }

        @SuppressWarnings("unused")
        public Object getContext(String key, Object data) {

            return subject;
        }

        public String[] getKeys() {

            return new String[] { "javax.security.auth.Subject.container" };
        }

        public boolean supports(String key) {

            if ("javax.security.auth.Subject.container".equals( key ))
                return true;
            return false;
        }
    }


    static class TestSessionContext implements SessionContext {

        private final Principal principal;

        private final String[] roles;

        public TestSessionContext(String principalName, String... roles) {

            if (null != principalName) {
                principal = new SimplePrincipal( principalName );
                this.roles = roles;
            } else {
                principal = null;
                this.roles = null;
            }

            TestPolicyContextHandler testPolicyContextHandler = new TestPolicyContextHandler( principal, roles );
            try {
                PolicyContext.registerHandler( "javax.security.auth.Subject.container", testPolicyContextHandler, true );
            } catch (PolicyContextException e) {
                throw new EJBException( "policy context error: " + e.getMessage() );
            }
        }

        public EJBLocalObject getEJBLocalObject()
                throws IllegalStateException {

            return null;
        }

        public EJBObject getEJBObject()
                throws IllegalStateException {

            return null;
        }

        public Class<?> getInvokedBusinessInterface()
                throws IllegalStateException {

            return null;
        }

        public MessageContext getMessageContext()
                throws IllegalStateException {

            return null;
        }

        @SuppressWarnings("deprecation")
        public java.security.Identity getCallerIdentity() {

            return null;
        }

        public Principal getCallerPrincipal() {

            if (null == principal)
                throw new EJBException( "caller principal not set" );
            return principal;
        }

        public EJBHome getEJBHome() {

            return null;
        }

        public EJBLocalHome getEJBLocalHome() {

            return null;
        }

        public Properties getEnvironment() {

            return null;
        }

        public boolean getRollbackOnly()
                throws IllegalStateException {

            return false;
        }

        public TimerService getTimerService()
                throws IllegalStateException {

            return null;
        }

        public UserTransaction getUserTransaction()
                throws IllegalStateException {

            return null;
        }

        @SuppressWarnings("deprecation")
        public boolean isCallerInRole(@SuppressWarnings("unused") java.security.Identity arg0) {

            return false;
        }

        public boolean isCallerInRole(String expectedRole) {

            if (null == roles)
                return false;
            for (String role : roles)
                if (true == role.equals( expectedRole ))
                    return true;
            return false;
        }

        public Object lookup(@SuppressWarnings("unused") String arg0) {

            return null;
        }

        public void setRollbackOnly()
                throws IllegalStateException {

        }

        public <T> T getBusinessObject(@SuppressWarnings("unused") Class<T> arg0)
                throws IllegalStateException {

            return null;
        }
    }


    static class TestTimer implements Timer {

        public void cancel()
                throws IllegalStateException, NoSuchObjectLocalException, EJBException {

        }

        public TimerHandle getHandle()
                throws IllegalStateException, NoSuchObjectLocalException, EJBException {

            return null;
        }

        public Serializable getInfo()
                throws IllegalStateException, NoSuchObjectLocalException, EJBException {

            return null;
        }

        public Date getNextTimeout()
                throws IllegalStateException, NoSuchObjectLocalException, EJBException {

            return null;
        }

        public long getTimeRemaining()
                throws IllegalStateException, NoSuchObjectLocalException, EJBException {

            return 0;
        }
    }


    static class TestTimerService implements TimerService {

        private static final Log serviceLOG = LogFactory.getLog( TestTimerService.class );

        @SuppressWarnings("unused")
        public Timer createTimer(long arg0, Serializable arg1)
                throws IllegalArgumentException, IllegalStateException, EJBException {

            return null;
        }

        @SuppressWarnings("unused")
        public Timer createTimer(Date arg0, Serializable arg1)
                throws IllegalArgumentException, IllegalStateException, EJBException {

            serviceLOG.debug( "createTimer" );
            Timer testTimer = new TestTimer();
            return testTimer;
        }

        @SuppressWarnings("unused")
        public Timer createTimer(long arg0, long arg1, Serializable arg2)
                throws IllegalArgumentException, IllegalStateException, EJBException {

            return null;
        }

        @SuppressWarnings("unused")
        public Timer createTimer(Date arg0, long arg1, Serializable arg2)
                throws IllegalArgumentException, IllegalStateException, EJBException {

            return null;
        }

        public Collection<?> getTimers()
                throws IllegalStateException, EJBException {

            return null;
        }
    }
}
