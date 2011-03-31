package net.link.util.config;

import com.google.common.collect.ImmutableList;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import net.link.util.common.KeyStoreUtils;


/**
 * <h2>{@link KeyProviderImpl}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>10 20, 2010</i> </p>
 *
 * @author lhunath
 */
public class KeyProviderImpl implements KeyProvider {

    private final PrivateKey            identityKey;
    private final List<X509Certificate> identityCertificateChain;
    private final List<X509Certificate> trustedCertificates;

    protected KeyProviderImpl(final KeyStore.PrivateKeyEntry identity, final List<X509Certificate> trustedCertificates) {

        this( identity.getPrivateKey(), ImmutableList.copyOf( (X509Certificate[]) identity.getCertificateChain() ), trustedCertificates );
    }

    protected KeyProviderImpl(final PrivateKey identityKey, final List<X509Certificate> identityCertificateChain,
                              final List<X509Certificate> trustedCertificates) {

        this.identityKey = identityKey;
        this.identityCertificateChain = identityCertificateChain;
        this.trustedCertificates = trustedCertificates;
    }

    public KeyPair getIdentityKeyPair() {

        return new KeyPair( getIdentityCertificate().getPublicKey(), identityKey );
    }

    @Override
    public X509Certificate getIdentityCertificate() {

        return KeyStoreUtils.getEndCertificate( getIdentityCertificateChain() );
    }

    public List<X509Certificate> getIdentityCertificateChain() {

        return identityCertificateChain;
    }

    public List<X509Certificate> getTrustedCertificates() {

        return trustedCertificates;
    }
}
