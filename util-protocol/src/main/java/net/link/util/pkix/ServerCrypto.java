/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.pkix;

import com.lyndir.lhunath.opal.system.logging.Logger;
import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.*;
import javax.security.auth.callback.CallbackHandler;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CryptoBase;
import org.apache.ws.security.components.crypto.CryptoType;
import org.jetbrains.annotations.Nullable;


public class ServerCrypto extends CryptoBase {

    private static final Logger logger = Logger.get( ServerCrypto.class );

    private X509Certificate certificate;

    @Override
    public X509Certificate loadCertificate(InputStream inputStream) {

        logger.dbg( "loadCertificate" );
        CertificateFactory certificateFactory;
        try {
            certificateFactory = CertificateFactory.getInstance( "X.509" );
        }
        catch (CertificateException e) {
            throw new InternalInconsistencyException( String.format( "cert error: %s", e.getMessage() ), e );
        }
        try {
            certificate = (X509Certificate) certificateFactory.generateCertificate( inputStream );
        }
        catch (CertificateException e) {
            throw new InternalInconsistencyException( String.format( "cert error: %s", e.getMessage() ), e );
        }
        return certificate;
    }

    @Override
    public X509Certificate[] getX509Certificates(final CryptoType cryptoType)
            throws WSSecurityException {

        // just return loaded certificate from WS-Security Header
        // real trust validation is done in the WSSecurityHandler's WSSecurityConfiguration instance
        logger.dbg( "getCertificates for cryptotype: %s", cryptoType );
        return new X509Certificate[] { certificate };
    }

    @Override
    public String getX509Identifier(final X509Certificate cert)
            throws WSSecurityException {

        return cert.getSerialNumber().toString();
    }

    @Nullable
    @Override
    public PrivateKey getPrivateKey(final X509Certificate certificate, final CallbackHandler callbackHandler)
            throws WSSecurityException {

        return null;
    }

    @Nullable
    @Override
    public PrivateKey getPrivateKey(final String identifier, final String password)
            throws WSSecurityException {

        return null;
    }

    @Deprecated
    @Override
    public boolean verifyTrust(final X509Certificate[] certs)
            throws WSSecurityException {

        return false;
    }

    @Override
    public boolean verifyTrust(final X509Certificate[] certs, final boolean enableRevocation)
            throws WSSecurityException {

        return false;
    }

    @Override
    public boolean verifyTrust(final PublicKey publicKey)
            throws WSSecurityException {

        return false;
    }
}
