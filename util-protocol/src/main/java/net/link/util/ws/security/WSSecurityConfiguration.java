package net.link.util.ws.security;

import java.security.PrivateKey;
import net.link.util.common.CertificateChain;
import org.joda.time.Duration;


/**
 * <h2>{@link WSSecurityConfiguration}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>03 30, 2011</i> </p>
 *
 * @author lhunath
 */
public interface WSSecurityConfiguration {

    /**
     * Given the calling entity's certificate, perform a verification of the digestion of the SOAP body element by the WS-Security
     * signature.
     *
     *
     * @param aCertificateChain The chain of the certificate that signed the message.
     *
     * @return <code>true</code> if incoming messages signed by the given certificate chain can be trusted.
     */
    boolean isCertificateChainTrusted(CertificateChain aCertificateChain);

    /**
     * @return the certificate chain that will be used to sign outgoing web service  messages.
     */
    CertificateChain getIdentityCertificateChain();

    /**
     * @return the private key which will be used to sign outgoing web service  messages.
     */
    PrivateKey getPrivateKey();

    Duration getMaximumAge();

    boolean isOutboundSignatureNeeded();

    boolean isInboundSignatureOptional();
}
