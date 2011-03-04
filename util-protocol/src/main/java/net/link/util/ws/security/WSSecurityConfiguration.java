/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.ws.security;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import org.joda.time.Duration;


/**
 * WS-Security configuration.
 *
 * @author lhunath
 */
public abstract class WSSecurityConfiguration {

    public static final Duration DEFAULT_MAX_AGE = new Duration( 1000 * 60 * 5L );

    /**
     * Given the calling entity's certificate, perform a verification of the digestion of the SOAP body element by the WS-Security
     * signature.
     *
     * @param certificateChain The chain of the certificate that signed the message.
     *
     * @return <code>true</code> if incoming messages signed by the given certificate chain can be trusted.
     */
    public abstract boolean isCertificateChainTrusted(Collection<X509Certificate> certificateChain);

    /**
     * @return the certificate chain that will be used to sign outgoing web service  messages.
     */
    public abstract List<X509Certificate> getCertificateChain();

    /**
     * @return the private key which will be used to sign outgoing web service  messages.
     */
    public abstract PrivateKey getPrivateKey();

    /**
     * @return the maximum age of the message.
     */
    public Duration getMaximumAge() {

        return DEFAULT_MAX_AGE;
    }

    public boolean isOutboundSignatureNeeded() {

        return true;
    }

    public boolean isInboundSignatureOptional() {

        return true;
    }
}
