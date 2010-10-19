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
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.components.crypto.Crypto;


/**
 * WSS4J Crypto implementation. This component hosts the client certificate and private key as used by the WSS4J library.
 *
 * @author fcorneli
 */
public class ClientCrypto implements Crypto {

    private static final Log LOG = LogFactory.getLog( ClientCrypto.class );

    private final X509Certificate certificate;

    private final PrivateKey privateKey;

    public ClientCrypto(X509Certificate certificate, PrivateKey privateKey) {

        this.certificate = certificate;
        this.privateKey = privateKey;
    }

    public String getAliasForX509Cert(Certificate cert) {

        return null;
    }

    public String getAliasForX509Cert(String issuer) {

        return null;
    }

    public String getAliasForX509Cert(byte[] subjectKeyIdentifier) {

        return null;
    }

    public String getAliasForX509Cert(String issuer, BigInteger serialNumber) {

        return null;
    }

    public String getAliasForX509CertThumb(byte[] thumb) {

        return null;
    }

    public String[] getAliasesForDN(String subjectDN) {

        return null;
    }

    public byte[] getCertificateData(boolean reverse, X509Certificate[] certificates) {

        return null;
    }

    public CertificateFactory getCertificateFactory() {

        return null;
    }

    public X509Certificate[] getCertificates(String alias) {

        LOG.debug( "getCertificates for alias: " + alias );
        X509Certificate[] certificates = new X509Certificate[] { certificate };
        return certificates;
    }

    public String getDefaultX509Alias() {

        return null;
    }

    public KeyStore getKeyStore() {

        return null;
    }

    public PrivateKey getPrivateKey(String alias, String password)
            throws Exception {

        LOG.debug( "getPrivateKey for alias: " + alias );
        return privateKey;
    }

    public byte[] getSKIBytesFromCert(X509Certificate cert) {

        return null;
    }

    public X509Certificate[] getX509Certificates(byte[] data, boolean reverse) {

        return null;
    }

    public X509Certificate loadCertificate(InputStream inputStream) {

        return null;
    }

    public boolean validateCertPath(X509Certificate[] certificates) {

        return false;
    }
}
