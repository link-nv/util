package net.link.util.config;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;


/**
 * <h2>{@link KeyProvider}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>10 20, 2010</i> </p>
 *
 * @author lhunath
 */
public interface KeyProvider {

    /**
     * @return The private and public key used by the this party to identify himself and authenticate his requests to the remote party.
     */
    KeyPair getIdentityKeyPair();

    /**
     * @return The certificate issued for the {@link #getIdentityKeyPair()}.
     */
    X509Certificate getIdentityCertificate();

    /**
     * @return The certificates that make up the a validatable chain to the certificate issued for the {@link #getIdentityKeyPair()}.  The remote
     *         party will validate and verify its trust in the chain.
     */
    List<X509Certificate> getIdentityCertificateChain();

    /**
     * @return The certificates that we trust.  The remote party's requests must provide a certificate chain that validates and is trusted
     *         by one of these certificates.
     */
    Collection<X509Certificate> getTrustedCertificates();

    /**
     * @return A trusted certificate with the given alias.
     */
    X509Certificate getTrustedCertificate(String alias);
}
