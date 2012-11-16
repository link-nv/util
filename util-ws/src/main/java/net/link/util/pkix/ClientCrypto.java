/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.pkix;

import com.google.common.collect.Iterables;
import com.lyndir.lhunath.opal.system.logging.Logger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import javax.security.auth.callback.CallbackHandler;
import net.link.util.common.CertificateChain;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CryptoBase;
import org.apache.ws.security.components.crypto.CryptoType;


/**
 * WSS4J Crypto implementation. This component hosts the client certificateChain and private key as used by the WSS4J library.
 *
 * @author fcorneli
 */
@SuppressWarnings("RefusedBequest")
public class ClientCrypto extends CryptoBase {

    private static final Logger logger = Logger.get( ClientCrypto.class );

    private final CertificateChain certificateChain;

    private final PrivateKey privateKey;

    public ClientCrypto(CertificateChain certificateChain, PrivateKey privateKey) {

        this.certificateChain = certificateChain;
        this.privateKey = privateKey;
    }

    @Override
    public X509Certificate[] getX509Certificates(final CryptoType cryptoType)
            throws WSSecurityException {

        logger.dbg( "getCertificates for cryptotype: %s", cryptoType );
        return Iterables.toArray( certificateChain.getOrderedCertificateChain(), X509Certificate.class );
    }

    @Override
    public String getX509Identifier(final X509Certificate cert)
            throws WSSecurityException {

        return cert.getSerialNumber().toString();
    }

    @Override
    public PrivateKey getPrivateKey(final X509Certificate certificate, final CallbackHandler callbackHandler)
            throws WSSecurityException {

        logger.dbg( "getPrivateKey for certificate: %s", certificate );
        return privateKey;
    }

    @Override
    public PrivateKey getPrivateKey(String alias, String password) {

        logger.dbg( "getPrivateKey for alias: %s", alias );
        return privateKey;
    }

    @Deprecated
    @Override
    public boolean verifyTrust(final X509Certificate[] certs)
            throws WSSecurityException {

        throw new UnsupportedOperationException();
    }

    @Override
    public boolean verifyTrust(final X509Certificate[] certs, final boolean enableRevocation)
            throws WSSecurityException {

        throw new UnsupportedOperationException();
    }

    @Override
    public boolean verifyTrust(final PublicKey publicKey)
            throws WSSecurityException {

        throw new UnsupportedOperationException();
    }
}
