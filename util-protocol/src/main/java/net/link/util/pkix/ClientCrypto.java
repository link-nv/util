/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.pkix;

import com.google.common.collect.Iterables;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import net.link.util.common.CertificateChain;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CryptoBase;
import org.jetbrains.annotations.Nullable;


/**
 * WSS4J Crypto implementation. This component hosts the client certificateChain and private key as used by the WSS4J library.
 *
 * @author fcorneli
 */
@SuppressWarnings("RefusedBequest")
public class ClientCrypto extends CryptoBase {

    private static final Log LOG = LogFactory.getLog( ClientCrypto.class );

    private final CertificateChain certificateChain;

    private final PrivateKey privateKey;

    public ClientCrypto(CertificateChain certificateChain, PrivateKey privateKey) {

        this.certificateChain = certificateChain;
        this.privateKey = privateKey;
    }

    @Nullable
    @Override
    protected String getCryptoProvider() {

        return null;
    }

    @Override
    public String getDefaultX509Alias() {

        throw new UnsupportedOperationException();
    }

    @Override
    public PrivateKey getPrivateKey(String alias, String password)
            throws Exception {

        LOG.debug( "getPrivateKey for alias: " + alias );
        return privateKey;
    }

    @Override
    public String getAliasForX509Cert(final String issuer)
            throws WSSecurityException {

        throw new UnsupportedOperationException();
    }

    @Override
    public String getAliasForX509Cert(final String issuer, final BigInteger serialNumber)
            throws WSSecurityException {

        throw new UnsupportedOperationException();
    }

    @Override
    public String getAliasForX509Cert(final byte[] skiBytes)
            throws WSSecurityException {

        throw new UnsupportedOperationException();
    }

    @Override
    public String getAliasForX509Cert(final Certificate cert)
            throws WSSecurityException {

        throw new UnsupportedOperationException();
    }

    @Override
    public String getAliasForX509CertThumb(final byte[] thumb)
            throws WSSecurityException {

        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getAliasesForDN(final String subjectDN)
            throws WSSecurityException {

        throw new UnsupportedOperationException();
    }

    @Override
    public boolean validateCertPath(final X509Certificate[] certs)
            throws WSSecurityException {

        throw new UnsupportedOperationException();
    }

    @Override
    public X509Certificate[] getCertificates(String alias) {

        LOG.debug( "getCertificates for alias: " + alias );
        return Iterables.toArray( certificateChain.getOrderedCertificateChain(), X509Certificate.class );
    }
}
