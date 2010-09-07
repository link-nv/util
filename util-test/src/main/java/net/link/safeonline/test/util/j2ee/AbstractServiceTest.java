/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.test.util.j2ee;

import java.lang.reflect.Field;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import net.link.safeonline.test.util.jpa.EntityTestManager;
import org.junit.After;
import org.junit.Before;


/**
 * <h2>{@link AbstractServiceTest}</h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Oct 16, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public abstract class AbstractServiceTest {

    protected EntityManager em;
    private EntityTestManager testEntityManager;


    @Before
    public void setup()
            throws Exception {

        testEntityManager = new EntityTestManager();
        testEntityManager.setUp( getEntities() );
        em = testEntityManager.getEntityManager();

        for (Field field : getClass().getDeclaredFields())
            if (field.isAnnotationPresent( EJB.class )) {
                field.setAccessible( true );
                field.set( this, EJBTestUtils.newInstance( field.getType(), getServices(), em ) );
            }
    }

    @After
    public void tearDown()
            throws Exception {

        testEntityManager.tearDown();
    }

    /**
     * @return All entity classes that are part of the test.
     */
    protected abstract Class<?>[] getEntities();

    /**
     * @return All service bean classes that are part of the test.
     */
    protected abstract Class<?>[] getServices();
}
