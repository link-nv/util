package test.unit.net.link.util.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import be.fedict.trust.TrustValidator;
import be.fedict.trust.linker.TrustLinkerResultException;
import be.fedict.trust.repository.MemoryCertificateRepository;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import net.link.util.common.CertificateChain;
import net.link.util.common.LazyPublicKeyTrustLinker;
import net.link.util.logging.Logger;


public class TrustTest {

    protected final Logger logger = Logger.get( getClass() );

    //    @Test
    public void testQRGeneration()
            throws Exception {

        // setup
        X509Certificate docdataCert = loadCertificate( TrustTest.class.getResourceAsStream( "/docdata.pem" ) );
        X509Certificate geotrust1Cert = loadCertificate( TrustTest.class.getResourceAsStream( "/geotrust1.pem" ) );
        X509Certificate geotrust2Cert = loadCertificate( TrustTest.class.getResourceAsStream( "/geotrust2.pem" ) );
        CertificateChain chain = new CertificateChain( geotrust1Cert, docdataCert, geotrust2Cert );

        X509Certificate trustedCertificate = loadCertificate( TrustTest.class.getResourceAsStream( "/equifax.pem" ) );

        // operate
        MemoryCertificateRepository certificateRepository = new MemoryCertificateRepository();
        certificateRepository.addTrustPoint( trustedCertificate );

        if (!chain.hasRootCertificate()) {
            // root certificate not included, take on assumption trustedCertificate == the rootCertificate
            chain.addRootCertificate( trustedCertificate );
        }

        // verify
        X509Certificate[] c = chain.toArray();
        assertEquals( docdataCert, c[0] );
        assertEquals( geotrust1Cert, c[1] );
        assertEquals( geotrust2Cert, c[2] );
        assertEquals( trustedCertificate, c[3] );

        assertEquals( trustedCertificate, chain.getRootCertificate() );

        // operate

        TrustValidator trustValidator = new TrustValidator( certificateRepository );
        trustValidator.addTrustLinker( new LazyPublicKeyTrustLinker() );

        try {
            trustValidator.isTrusted( chain.getOrderedCertificateChain() );
        }
        catch (TrustLinkerResultException e) {
            logger.err( e, e.getMessage() );
            fail();
        }
    }

    public static X509Certificate loadCertificate(InputStream inputStream)
            throws CertificateException {

        CertificateFactory certificateFactory = CertificateFactory.getInstance( "X.509" );
        return (X509Certificate) certificateFactory.generateCertificate( inputStream );
    }
}
