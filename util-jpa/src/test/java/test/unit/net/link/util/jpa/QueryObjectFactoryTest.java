/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.util.jpa;

import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;
import javax.persistence.*;
import net.link.util.jpa.QueryObjectFactory;
import net.link.util.test.jpa.EntityTestManager;
import org.junit.*;


public class QueryObjectFactoryTest {

    private EntityTestManager entityTestManager;

    private EntityManager entityManager;

    @Before
    public void setUp()
            throws Exception {

        entityTestManager = new EntityTestManager();
        entityTestManager.setUp( MyTestEntity.class );
        entityManager = entityTestManager.getEntityManager();
    }

    @After
    public void tearDown()
            throws Exception {

        entityTestManager.tearDown();
    }

    @Test
    public void createQueryObject()
            throws Exception {

        // Test
        MyTestEntity.MyQueryTestInterface queryObject = QueryObjectFactory.createQueryObject( entityManager,
                MyTestEntity.MyQueryTestInterface.class );

        // Verify
        assertNotNull( queryObject );
    }

    @Test
    public void simpleQueryWithEmptyResult()
            throws Exception {

        // Setup Data
        MyTestEntity.MyQueryTestInterface queryObject = QueryObjectFactory.createQueryObject( entityManager,
                MyTestEntity.MyQueryTestInterface.class );

        // Test
        List<MyTestEntity> result = queryObject.listAll();

        // Verify
        assertNotNull( result );
        assertTrue( result.isEmpty() );
    }

    @Test
    public void simpleQuery()
            throws Exception {

        // Setup Data
        String testName = UUID.randomUUID().toString();
        MyTestEntity myTestEntity = new MyTestEntity( testName );
        entityManager.persist( myTestEntity );

        MyTestEntity.MyQueryTestInterface queryObject = QueryObjectFactory.createQueryObject( entityManager,
                MyTestEntity.MyQueryTestInterface.class );

        // Test
        List<MyTestEntity> result = queryObject.listAll();

        // Verify
        assertNotNull( result );
        assertEquals( 1, result.size() );
        assertEquals( testName, result.get( 0 ).getName() );
    }

    @Test
    public void queryWithParam()
            throws Exception {

        // Setup Data
        String testName = UUID.randomUUID().toString();
        MyTestEntity myTestEntity = new MyTestEntity( testName );
        entityManager.persist( myTestEntity );

        MyTestEntity.MyQueryTestInterface queryObject = QueryObjectFactory.createQueryObject( entityManager,
                MyTestEntity.MyQueryTestInterface.class );

        // Test
        List<MyTestEntity> result = queryObject.listAll( testName );

        // Verify
        assertNotNull( result );
        assertEquals( 1, result.size() );
        assertEquals( testName, result.get( 0 ).getName() );

        // Test
        result = queryObject.listAll( testName + "foobar" );

        // Verify
        assertNotNull( result );
        assertTrue( result.isEmpty() );
    }

    @Test
    public void singleResultQueryWithParam()
            throws Exception {

        // Setup Data
        String testName = UUID.randomUUID().toString();
        MyTestEntity myTestEntity = new MyTestEntity( testName );
        entityManager.persist( myTestEntity );

        MyTestEntity.MyQueryTestInterface queryObject = QueryObjectFactory.createQueryObject( entityManager,
                MyTestEntity.MyQueryTestInterface.class );

        // Test
        MyTestEntity result = queryObject.get( testName );

        // Verify
        assertNotNull( result );
        assertEquals( testName, result.getName() );

        // Test
        try {
            queryObject.get( testName + "foobar" );
            fail();
        }
        catch (NoResultException e) {
            // Expected
        }
    }

    @Test
    public void nullableSingleResultQueryWithParam()
            throws Exception {

        // Setup Data
        String testName = UUID.randomUUID().toString();
        MyTestEntity myTestEntity = new MyTestEntity( testName );
        entityManager.persist( myTestEntity );

        MyTestEntity.MyQueryTestInterface queryObject = QueryObjectFactory.createQueryObject( entityManager,
                MyTestEntity.MyQueryTestInterface.class );

        // Test
        MyTestEntity result = queryObject.get( testName );

        // Verify
        assertNotNull( result );
        assertEquals( testName, result.getName() );

        // Test
        result = queryObject.find( testName + "foobar" );

        // Verify
        assertNull( result );
    }

    @Test
    public void updateMethod()
            throws Exception {

        // Setup Data
        String testName = UUID.randomUUID().toString();
        MyTestEntity myTestEntity = new MyTestEntity( testName );
        entityManager.persist( myTestEntity );

        MyTestEntity.MyQueryTestInterface queryObject = QueryObjectFactory.createQueryObject( entityManager,
                MyTestEntity.MyQueryTestInterface.class );

        // Test
        queryObject.removeAll();
        queryObject.removeAllReturningInt();
        queryObject.removeAllReturningInteger();

        // Test
        MyTestEntity result = queryObject.find( testName );

        // Verify
        assertNull( result );
    }

    @Test
    public void queryQueryMethod()
            throws Exception {

        // Setup Data
        MyTestEntity.MyQueryTestInterface queryObject = QueryObjectFactory.createQueryObject( entityManager,
                MyTestEntity.MyQueryTestInterface.class );

        // Test
        Query result = queryObject.listAllQuery();

        // Verify
        assertNotNull( result );
    }

    @Test
    public void countQuery()
            throws Exception {

        // Setup Data
        MyTestEntity.MyQueryTestInterface queryObject = QueryObjectFactory.createQueryObject( entityManager,
                MyTestEntity.MyQueryTestInterface.class );

        // Test
        long count = queryObject.countAll();

        // Verify
        assertTrue( 0 == count );
    }
}
