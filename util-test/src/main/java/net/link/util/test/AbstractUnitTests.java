/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.test;

import static org.easymock.EasyMock.*;

import java.util.*;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import net.link.util.j2ee.EJBUtils;
import net.link.util.j2ee.FieldNamingStrategy;
import net.link.util.test.j2ee.EJBTestUtils;
import net.link.util.test.j2ee.JNDITestUtils;
import net.link.util.test.jpa.EntityTestManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.*;


/**
 * <h2>{@link AbstractUnitTests}</h2>
 * <p/>
 * <p>
 * <i>Nov 26, 2009</i>
 * </p>
 *
 * @author lhunath
 */
public abstract class AbstractUnitTests<T> {

    protected static final Random random = new Random();

    protected static JNDITestUtils     jndiTestUtils;
    protected static EntityTestManager entityTestManager;

    protected final Log LOG = LogFactory.getLog( getClass() );

    protected EntityManager      entityManager;
    protected T                  testedBean;
    protected Class<? extends T> testedClass;

    public AbstractUnitTests() {

    }

    public AbstractUnitTests(Class<? extends T> testedClass) {

        this.testedClass = testedClass;
    }

    /**
     * Builds the JNDI context and the {@link EntityTestManager}.
     */
    @BeforeClass
    public static void init()
            throws Exception {

        jndiTestUtils = new JNDITestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.setNamingStrategy( new FieldNamingStrategy() );

        entityTestManager = new EntityTestManager();
    }

    /**
     * Creates an entity manager, loads the entities from {@link #getEntities()} (unless that returns <code>null</code>) and loads all the
     * service beans from {@link #getServiceBeans()} (unless that returns <code>null</code>) into the JNDI.
     * <p/>
     * <p>
     * If you want to use a non-HSQL data source, use {@link EntityTestManager#configureMySql(String, int, String, String, String,
     * boolean)}
     * or before calling this method.
     * </p>
     */
    @Before
    public void setUp()
            throws Exception {

        LOG.debug( "=== <SET-UP> ===" );

        // Set up an HSQL entity manager.
        Class<?>[] entities = getEntities();
        if (entities != null)
            try {
                entityTestManager.setUp( entities );
            }
            catch (Exception err) {
                LOG.error( "Couldn't set up entity manager", err );
                throw new IllegalStateException( err );
            }
        entityManager = entityTestManager.getEntityManager();

        // Bind our mocks into the JNDI.
        Object[] mocks = getMocks();
        if (mocks != null)
            for (Object mock : mocks)
                bindMock( mock );

        // Bind our service beans into the JNDI.
        Class<?>[] serviceBeanArray = getServiceBeans();
        if (serviceBeanArray == null)
            serviceBeanArray = new Class<?>[0];
        LinkedList<Class<?>> serviceBeans = new LinkedList<Class<?>>( Arrays.asList( serviceBeanArray ) );
        if (null != testedClass && !testedClass.isInterface() && !serviceBeans.contains( testedClass ))
            serviceBeans.add( testedClass );
        for (Class<?> beanClass : serviceBeans) {

            Object serviceBean = EJBTestUtils.newInstance( beanClass, serviceBeanArray, entityManager );
            jndiTestUtils.bindComponent( beanClass, serviceBean );

            if (null != testedClass)
                if (testedClass.isAssignableFrom( beanClass ))
                    testedBean = testedClass.cast( serviceBean );
        }

        LOG.debug( "=== </SET-UP> ===" );
    }

    /**
     * Tears down the {@link EntityTestManager} and JNDI context.
     */
    @After
    public void tearDown()
            throws Exception {

        LOG.debug( "=== <TEAR-DOWN> ===" );

        if (entityTestManager != null)
            entityTestManager.tearDown();
        if (jndiTestUtils != null)
            jndiTestUtils.tearDown();

        LOG.debug( "=== </TEAR-DOWN> ===" );
    }

    // Utilities

    /**
     * @return The EJB from the JNDI that implements the given interface and is bound on that interface's JNDI_BINDING.
     */
    protected static <B> B ejb(Class<B> beanInterface) {

        return EJBUtils.getEJB( beanInterface );
    }

    /**
     * Bind the given mock object in the JNDI on its mock interface's JNDI_BINDING.
     */
    protected static <M> M createAndBindMock(Class<M> mock)
            throws NamingException {

        return bindMock( createMock( mock ) );
    }

    /**
     * Bind the given mock object in the JNDI on its mock interface's JNDI_BINDING.
     */
    protected static <M> M bindMock(M mock)
            throws NamingException {

        jndiTestUtils.bindComponent( mock.getClass().getInterfaces()[0], mock );

        return mock;
    }

    // Data

    /**
     * Implement this method if your artifact provides service beans that need to be loaded into the JNDI.
     *
     * @return All the service beans that are used by the wicket application that is being tested.
     */
    protected Class<?>[] getServiceBeans() {

        return null;
    }

    /**
     * Implement this method if your artifact provides service beans that need to be loaded into the JNDI.
     *
     * @return All the service beans that are used by the wicket application that is being tested.
     */
    protected Object[] getMocks() {

        return null;
    }

    /**
     * Implement this method if your artifact uses persistence.
     *
     * @return All the entity beans that are used by the wicket application that is being tested.
     */
    protected Class<?>[] getEntities() {

        return null;
    }
}
