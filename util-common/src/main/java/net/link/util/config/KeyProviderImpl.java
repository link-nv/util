package net.link.util.config;

import com.google.common.collect.ImmutableList;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Map;
import net.link.util.common.CertificateChain;


/**
 * <h2>{@link KeyProviderImpl}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>10 20, 2010</i> </p>
 *
 * @author lhunath
 */
public class KeyProviderImpl implements KeyProvider {

    private final PrivateKey                   identityKey;
    private final CertificateChain             identityCertificateChain;
    private final Map<String, X509Certificate> trustedCertificates;

    /**
     * @param identity            The entry that specifies the identity's keys.
     * @param trustedCertificates The certificates of remote entities that we trust.
     */
    public KeyProviderImpl(final KeyStore.PrivateKeyEntry identity, final Map<String, X509Certificate> trustedCertificates) {

        this( identity.getPrivateKey(), ImmutableList.copyOf( (X509Certificate[]) identity.getCertificateChain() ), trustedCertificates );
    }

    public KeyProviderImpl(final PrivateKey identityKey, final Collection<X509Certificate> identityCertificateChain,
                           final Map<String, X509Certificate> trustedCertificates) {

        this.identityKey = identityKey;
        this.identityCertificateChain = new CertificateChain( identityCertificateChain );
        this.trustedCertificates = trustedCertificates;
    }

    public KeyPair getIdentityKeyPair() {

        return new KeyPair( getIdentityCertificate().getPublicKey(), identityKey );
    }

    @Override
    public X509Certificate getIdentityCertificate() {

        return getIdentityCertificateChain().getIdentityCertificate();
    }

    public CertificateChain getIdentityCertificateChain() {

        return identityCertificateChain;
    }

    public Collection<X509Certificate> getTrustedCertificates() {

        return trustedCertificates.values();
    }

    @Override
    public X509Certificate getTrustedCertificate(final String alias) {

        return trustedCertificates.get( alias );
    }
}
