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
import java.util.*;
import net.link.util.common.CertificateChain;
import net.link.util.test.pkix.PkiTestUtils;
import org.joda.time.DateTime;
import org.junit.*;


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
    public void testOrderCertificateChainUnordered()
            throws Exception {

        // Setup
        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusYears( 1 );

        KeyPair rootKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate rootCertificate = PkiTestUtils.generateSelfSignedCertificate( rootKeyPair, "CN=Root" );

        KeyPair ca1KeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate ca1Certificate = PkiTestUtils.generateCertificate( ca1KeyPair.getPublic(), "CN=CA1", rootKeyPair.getPrivate(),
                rootCertificate, notBefore, notAfter, null, true, true, false, null );

        KeyPair ca2KeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate ca2Certificate = PkiTestUtils.generateCertificate( ca2KeyPair.getPublic(), "CN=CA2", ca1KeyPair.getPrivate(),
                ca1Certificate, notBefore, notAfter, null, true, true, false, null );

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate certificate = PkiTestUtils.generateCertificate( keyPair.getPublic(), "CN=Test", ca2KeyPair.getPrivate(),
                ca2Certificate, notBefore, notAfter, null, true, false, false, null );

        CertificateChain certificateChain = new CertificateChain(
                Arrays.asList( ca1Certificate, certificate, rootCertificate, ca2Certificate ) );

        // Verify
        assertEquals( certificate, certificateChain.getIdentityCertificate() );
        assertEquals( ca2Certificate, certificateChain.getOrderedCertificateChain().get( 1 ) );
        assertEquals( ca1Certificate, certificateChain.getOrderedCertificateChain().get( 2 ) );
        assertEquals( rootCertificate, certificateChain.getRootCertificate() );
    }

    @Test
    public void testOrderCertificateChainOrdered()
            throws Exception {

        // Setup
        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusYears( 1 );

        KeyPair rootKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate rootCertificate = PkiTestUtils.generateSelfSignedCertificate( rootKeyPair, "CN=Root" );

        KeyPair ca1KeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate ca1Certificate = PkiTestUtils.generateCertificate( ca1KeyPair.getPublic(), "CN=CA1", rootKeyPair.getPrivate(),
                rootCertificate, notBefore, notAfter, null, true, true, false, null );

        KeyPair ca2KeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate ca2Certificate = PkiTestUtils.generateCertificate( ca2KeyPair.getPublic(), "CN=CA2", ca1KeyPair.getPrivate(),
                ca1Certificate, notBefore, notAfter, null, true, true, false, null );

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate certificate = PkiTestUtils.generateCertificate( keyPair.getPublic(), "CN=Test", ca2KeyPair.getPrivate(),
                ca2Certificate, notBefore, notAfter, null, true, false, false, null );

        CertificateChain certificateChain = new CertificateChain(
                Arrays.asList( certificate, ca2Certificate, ca1Certificate, rootCertificate ) );

        // Verify
        assertEquals( certificate, certificateChain.getIdentityCertificate() );
        assertEquals( ca2Certificate, certificateChain.getOrderedCertificateChain().get( 1 ) );
        assertEquals( ca1Certificate, certificateChain.getOrderedCertificateChain().get( 2 ) );
        assertEquals( rootCertificate, certificateChain.getRootCertificate() );
    }

    @Test
    public void testOrderCertificateChainEmpty()
            throws Exception {

        // Operate: sort
        CertificateChain certificateChain = new CertificateChain( new LinkedList<X509Certificate>() );

        // Verify
        assertTrue( certificateChain.getOrderedCertificateChain().isEmpty() );
    }

    @Test
    public void testOrderCertificateChain1Element()
            throws Exception {

        // Setup
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate( keyPair, "CN=Test" );

        // Operate: sort
        CertificateChain certificateChain = new CertificateChain( Collections.singletonList( certificate ) );

        // Verify
        assertEquals( 1, certificateChain.getOrderedCertificateChain().size() );
        assertEquals( certificate, certificateChain.getIdentityCertificate() );
    }

    @Test
    public void testOrderCertificateChain2Elements()
            throws Exception {

        // Setup
        DateTime notBefore = new DateTime();
        DateTime notAfter = notBefore.plusYears( 1 );

        KeyPair rootKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate rootCertificate = PkiTestUtils.generateSelfSignedCertificate( rootKeyPair, "CN=Root" );

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate certificate = PkiTestUtils.generateCertificate( keyPair.getPublic(), "CN=Test", rootKeyPair.getPrivate(),
                rootCertificate, notBefore, notAfter, null, true, false, false, null );

        CertificateChain certificateChain = new CertificateChain( Arrays.asList( rootCertificate, certificate ) );

        // Verify
        assertEquals( 2, certificateChain.getOrderedCertificateChain().size() );
        assertEquals( certificate, certificateChain.getIdentityCertificate() );
        assertEquals( rootCertificate, certificateChain.getRootCertificate() );
    }
}
