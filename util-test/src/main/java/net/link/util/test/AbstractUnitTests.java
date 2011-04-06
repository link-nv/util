/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.test;

import com.lyndir.lhunath.lib.system.logging.Logger;
import java.util.*;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import net.link.util.j2ee.EJBUtils;
import net.link.util.j2ee.FieldNamingStrategy;
import net.link.util.test.j2ee.EJBTestUtils;
import net.link.util.test.j2ee.JNDITestUtils;
import net.link.util.test.jpa.EntityTestManager;
import org.easymock.EasyMock;
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

    protected final Logger logger = Logger.get( getClass() );

    protected static final Random random = new Random();

    protected static final JNDITestUtils     jndiTestUtils     = new JNDITestUtils();
    protected static final EntityTestManager entityTestManager = new EntityTestManager();

    protected static final List<Object> mocks = new LinkedList<Object>();

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

        jndiTestUtils.setUp();
        jndiTestUtils.setNamingStrategy( new FieldNamingStrategy() );
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
    public final void setUp()
            throws Exception {

        logger.dbg( "=== <SET-UP> ===" );

        // Set up an HSQL entity manager.
        Class<?>[] entities = getEntities();
        if (entities != null)
            try {
                entityTestManager.setUp( entities );
            }
            catch (Exception err) {
                logger.err( err, "Couldn't set up entity manager" );
                throw new IllegalStateException( err );
            }
        entityManager = entityTestManager.getEntityManager();

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

        _setUp();

        logger.dbg( "=== </SET-UP> ===" );
    }

    protected void _setUp()
            throws Exception {

        replayMocks();
    }

    /**
     * Tears down the {@link EntityTestManager} and JNDI context.
     */
    @After
    public final void tearDown()
            throws Exception {

        logger.dbg( "=== <TEAR-DOWN> ===" );

        _tearDown();

        resetMocks();

        if (entityTestManager != null)
            entityTestManager.tearDown();
        if (jndiTestUtils != null)
            jndiTestUtils.tearDown();

        logger.dbg( "=== </TEAR-DOWN> ===" );
    }

    protected void _tearDown()
            throws Exception {

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
    protected static <M> M createAndBindMock(Class<M> mockType) {

        return bindMock( createMock( mockType ) );
    }

    /**
     * Bind the given mock object in the JNDI on its mock interface's JNDI_BINDING.
     */
    protected static <M> M createMock(Class<M> mockType) {

        M mock = EasyMock.createMock( mockType );
        mocks.add( mock );

        return mock;
    }

    protected static List<Object> getMocks() {

        return mocks;
    }

    public static void reset(Void... args) {

        // Not allowed!  Use #resetMocks instead.
    }

    protected static void resetMocks() {

        EasyMock.reset( mocks.toArray() );
    }

    public static void replay(Void... args) {

        // Not allowed!  Use #replayMocks instead.
    }

    protected static void replayMocks() {

        EasyMock.replay( mocks.toArray() );
    }

    public static void verify(Void... args) {

        // Not allowed!  Use #verifyMocks instead.
    }

    protected static void verifyMocks() {

        EasyMock.verify( mocks.toArray() );
    }

    /**
     * Bind the given mock object in the JNDI on its mock interface's JNDI_BINDING.
     */
    protected static <M> M bindMock(M mock) {

        try {
            jndiTestUtils.bindComponent( mock.getClass().getInterfaces()[0], mock );
        }
        catch (NamingException e) {
            throw new RuntimeException( e );
        }

        return mock;
    }

    // Data

    /**
     * Implement this method if your artifact provides service beans that need to be loaded into the JNDI.
     *
     * @return All the service beans that are used by the tests.
     */
    protected Class<?>[] getServiceBeans() {

        return null;
    }

    /**
     * Implement this method if your artifact uses persistence.
     *
     * @return All the entity beans that are used by the tests.
     */
    protected Class<?>[] getEntities() {

        return null;
    }
}
