package net.link.util.ws.security;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
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
     * @param certificateChain The chain of the certificate that signed the message.
     *
     * @return <code>true</code> if incoming messages signed by the given certificate chain can be trusted.
     */
    boolean isCertificateChainTrusted(Collection<X509Certificate> certificateChain);

    /**
     * @return the certificate chain that will be used to sign outgoing web service  messages.
     */
    List<X509Certificate> getCertificateChain();

    /**
     * @return the private key which will be used to sign outgoing web service  messages.
     */
    PrivateKey getPrivateKey();

    Duration getMaximumAge();

    boolean isOutboundSignatureNeeded();

    boolean isInboundSignatureOptional();
}
