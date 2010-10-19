/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.ws.pkix;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.components.crypto.Crypto;


public class ServerCrypto implements Crypto {

    private static final Log LOG = LogFactory.getLog( ServerCrypto.class );

    @SuppressWarnings("unused")
    public String getAliasForX509Cert(Certificate certificate) {

        return null;
    }

    @SuppressWarnings("unused")
    public String getAliasForX509Cert(String issuer) {

        return null;
    }

    @SuppressWarnings("unused")
    public String getAliasForX509Cert(byte[] subjectKeyIdentifier) {

        return null;
    }

    @SuppressWarnings("unused")
    public String getAliasForX509Cert(String issuer, BigInteger serialNumber) {

        return null;
    }

    @SuppressWarnings("unused")
    public String getAliasForX509CertThumb(byte[] thumb) {

        return null;
    }

    @SuppressWarnings("unused")
    public String[] getAliasesForDN(String subjectDN) {

        return null;
    }

    @SuppressWarnings("unused")
    public byte[] getCertificateData(boolean reverse, X509Certificate[] certificates) {

        return null;
    }

    public CertificateFactory getCertificateFactory() {

        return null;
    }

    @SuppressWarnings("unused")
    public X509Certificate[] getCertificates(String alias) {

        return null;
    }

    public String getDefaultX509Alias() {

        return null;
    }

    public KeyStore getKeyStore() {

        return null;
    }

    @SuppressWarnings("unused")
    public PrivateKey getPrivateKey(String alias, String password)
            throws Exception {

        return null;
    }

    @SuppressWarnings("unused")
    public byte[] getSKIBytesFromCert(X509Certificate certificate) {

        return null;
    }

    @SuppressWarnings("unused")
    public X509Certificate[] getX509Certificates(byte[] data, boolean reverse) {

        return null;
    }

    public X509Certificate loadCertificate(InputStream inputStream) {

        LOG.debug( "loadCertificate" );
        CertificateFactory certificateFactory;
        try {
            certificateFactory = CertificateFactory.getInstance( "X.509" );
        } catch (CertificateException e) {
            throw new RuntimeException( "cert error: " + e.getMessage() );
        }
        X509Certificate certificate;
        try {
            certificate = (X509Certificate) certificateFactory.generateCertificate( inputStream );
        } catch (CertificateException e) {
            throw new RuntimeException( "cert error: " + e.getMessage() );
        }
        return certificate;
    }

    @SuppressWarnings("unused")
    public boolean validateCertPath(X509Certificate[] certificates) {

        return false;
    }
}
