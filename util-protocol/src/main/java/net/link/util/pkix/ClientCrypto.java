/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.pkix;

import com.google.common.collect.Iterables;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.components.crypto.Crypto;


/**
 * WSS4J Crypto implementation. This component hosts the client certificateChain and private key as used by the WSS4J library.
 *
 * @author fcorneli
 */
public class ClientCrypto implements Crypto {

    private static final Log LOG = LogFactory.getLog( ClientCrypto.class );

    private final List<X509Certificate> certificateChain;

    private final PrivateKey privateKey;

    public ClientCrypto(List<X509Certificate> certificateChain, PrivateKey privateKey) {

        this.certificateChain = certificateChain;
        this.privateKey = privateKey;
    }

    public String getAliasForX509Cert(Certificate cert) {

        throw new UnsupportedOperationException();
    }

    public String getAliasForX509Cert(String issuer) {

        throw new UnsupportedOperationException();
    }

    public String getAliasForX509Cert(byte[] subjectKeyIdentifier) {

        throw new UnsupportedOperationException();
    }

    public String getAliasForX509Cert(String issuer, BigInteger serialNumber) {

        throw new UnsupportedOperationException();
    }

    public String getAliasForX509CertThumb(byte[] thumb) {

        throw new UnsupportedOperationException();
    }

    public String[] getAliasesForDN(String subjectDN) {

        throw new UnsupportedOperationException();
    }

    public byte[] getCertificateData(boolean reverse, X509Certificate[] certificates) {

        throw new UnsupportedOperationException();
    }

    public CertificateFactory getCertificateFactory() {

        throw new UnsupportedOperationException();
    }

    public X509Certificate[] getCertificates(String alias) {

        LOG.debug( "getCertificates for alias: " + alias );
        return Iterables.toArray( certificateChain, X509Certificate.class );
    }

    public String getDefaultX509Alias() {

        throw new UnsupportedOperationException();
    }

    public KeyStore getKeyStore() {

        throw new UnsupportedOperationException();
    }

    public PrivateKey getPrivateKey(String alias, String password)
            throws Exception {

        LOG.debug( "getPrivateKey for alias: " + alias );
        return privateKey;
    }

    public byte[] getSKIBytesFromCert(X509Certificate cert) {

        throw new UnsupportedOperationException();
    }

    public X509Certificate[] getX509Certificates(byte[] data, boolean reverse) {

        throw new UnsupportedOperationException();
    }

    public X509Certificate loadCertificate(InputStream inputStream) {

        throw new UnsupportedOperationException();
    }

    public boolean validateCertPath(X509Certificate[] certificates) {

        throw new UnsupportedOperationException();
    }
}
