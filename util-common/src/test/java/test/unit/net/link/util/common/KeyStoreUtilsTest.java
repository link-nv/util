/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.util.common;

import static org.junit.Assert.*;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import net.link.util.common.KeyStoreUtils;
import net.link.util.test.pkix.PkiTestUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class KeyStoreUtilsTest {

    @Before
    public void setUp()
            throws Exception {

    }

    @After
    public void tearDown()
            throws Exception {

    }

    @Test
    public void testOrderCertificateChain()
            throws Exception {

        // Setup
        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusYears( 1 );

        KeyPair rootKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate rootCertificate = PkiTestUtils.generateSelfSignedCertificate( rootKeyPair, "CN=Root" );

        KeyPair ca1KeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate ca1Certificate = PkiTestUtils.generateCertificate( ca1KeyPair.getPublic(), "CN=CA1", rootKeyPair.getPrivate(),
                                                                           rootCertificate, notBefore, notAfter, null, true, true, false,
                                                                           null );

        KeyPair ca2KeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate ca2Certificate = PkiTestUtils.generateCertificate( ca2KeyPair.getPublic(), "CN=CA2", ca1KeyPair.getPrivate(),
                                                                           ca1Certificate, notBefore, notAfter, null, true, true, false,
                                                                           null );

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate certificate = PkiTestUtils.generateCertificate( keyPair.getPublic(), "CN=Test", ca2KeyPair.getPrivate(),
                                                                        ca2Certificate, notBefore, notAfter, null, true, false, false,
                                                                        null );

        List<X509Certificate> certificateChain = Arrays.asList( ca1Certificate, certificate, rootCertificate, ca2Certificate );

        // Operate: sort
        List<X509Certificate> orderedCertChain = KeyStoreUtils.getOrderedCertificateChain( certificateChain );

        // Verify
        assertNotNull( orderedCertChain );
        assertEquals( certificate, orderedCertChain.get( 0 ) );
        assertEquals( ca2Certificate, orderedCertChain.get( 1 ) );
        assertEquals( ca1Certificate, orderedCertChain.get( 2 ) );
        assertEquals( rootCertificate, orderedCertChain.get( 3 ) );
    }

    @Test
    public void testOrderCertificateChainEmpty()
            throws Exception {

        // Operate: sort
        List<X509Certificate> orderedCertChain = KeyStoreUtils.getOrderedCertificateChain( new LinkedList<X509Certificate>() );

        // Verify
        assertTrue( orderedCertChain.isEmpty() );
    }

    @Test
    public void testOrderCertificateChain1Element()
            throws Exception {

        // Setup
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate( keyPair, "CN=Test" );

        // Operate: sort
        List<X509Certificate> orderedCertChain = KeyStoreUtils.getOrderedCertificateChain( Collections.singletonList( certificate ) );

        // Verify
        assertEquals( 1, orderedCertChain.size() );
        assertEquals( certificate, orderedCertChain.get( 0 ) );
    }
}
