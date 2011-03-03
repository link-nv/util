/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.ws.pkix.wssecurity;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;


/**
 * WS-Security configuration service interface.
 *
 * @author wvdhaute
 */
public interface WSSecurityConfigurationService {

    /**
     * Returns the maximum offset of the WS-Security timestamp.
     */
    long getMaximumWsSecurityTimestampOffset();

    /**
     * Given the calling entity's certificate, perform a verification of the digestion of the SOAP body element by the WS-Security
     * signature.
     */
    boolean isCertificateChainValid(Collection<X509Certificate> certificateChain);

    /**
     * Returns the certificate chain that will be used to sign web service response messages.
     */
    List<X509Certificate> getCertificateChain();

    /**
     * Returns the private key which will be used to sign web service response messages.
     */
    PrivateKey getPrivateKey();
}
