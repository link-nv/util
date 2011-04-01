/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.util.test;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import junit.framework.TestCase;
import net.link.util.test.j2ee.JNDITestUtils;


/**
 * Who will guard the guards.
 *
 * @author fcorneli
 */
@SuppressWarnings( { "JNDIResourceOpenedButNotSafelyClosed" })
public class JNDITestUtilsTest extends TestCase {

    public void testBindComponent()
            throws Exception {

        // Setup Data
        Object testComponent = new Object();
        String testJndiName = "test/jndi/name";

        // Test
        JNDITestUtils testedInstance = new JNDITestUtils();
        testedInstance.setUp();
        testedInstance.bindComponent( testJndiName, testComponent );

        // Verify
        InitialContext initialContext = new InitialContext();
        Object result = initialContext.lookup( testJndiName );
        assertEquals( testComponent, result );

        // operate & verify
        testedInstance.tearDown();
        try {
            initialContext.lookup( testJndiName );
            fail();
        }
        catch (NameNotFoundException e) {
            // Expected
        }
    }

    public void testBindComponentWithSimpleName()
            throws Exception {

        // Setup Data
        Object testComponent = new Object();
        String testSimpleJndiName = "simpleName";

        // Test
        JNDITestUtils testedInstance = new JNDITestUtils();
        testedInstance.setUp();
        testedInstance.bindComponent( testSimpleJndiName, testComponent );

        // Verify
        InitialContext initialContext = new InitialContext();
        Object result = initialContext.lookup( testSimpleJndiName );
        assertEquals( testComponent, result );

        // operate & verify
        testedInstance.tearDown();
        try {
            initialContext.lookup( testSimpleJndiName );
            fail();
        }
        catch (NameNotFoundException e) {
            // Expected
        }
    }
}
