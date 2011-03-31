package net.link.util.config;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;
import com.lyndir.lhunath.lib.system.util.ObjectUtils;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import net.link.util.common.KeyStoreUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link KeyStoreKeyProvider}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>10 20, 2010</i> </p>
 *
 * @author lhunath
 */
public class KeyStoreKeyProvider extends KeyProviderImpl {

    public static final String IDENTITY_ALIAS = "identity";

    public KeyStoreKeyProvider(@NotNull KeyStore keyStore) {

        this( keyStore, null, null );
    }

    protected KeyStoreKeyProvider(@NotNull KeyStore keyStore, @Nullable String keyEntryAlias, @Nullable String keyEntryPassword) {

        super( getIdentity( keyStore, keyEntryAlias, keyEntryPassword ),
                ImmutableList.copyOf( KeyStoreUtils.getCertificates( keyStore, null ).values() ) );
    }

    private static KeyStore.PrivateKeyEntry getIdentity(final KeyStore keyStore, final String keyEntryAlias,
                                                        final String keyEntryPassword) {

        try {
            KeyStore.Entry entry = keyStore.getEntry( ObjectUtils.getOrDefault( keyEntryAlias, IDENTITY_ALIAS ), //
                    new KeyStore.PasswordProtection( keyEntryPassword.toCharArray() ) );

            checkState( entry instanceof KeyStore.PrivateKeyEntry, "Identity entry in the key store should be a private key" );
            return (KeyStore.PrivateKeyEntry) entry;
        }
        catch (GeneralSecurityException e) {
            throw new RuntimeException( e );
        }
    }

    protected static KeyStore loadKeyStore(InputSupplier<? extends InputStream> streamSupplier, String keyStorePassword) {

        try {
            InputStream stream = streamSupplier.getInput();

            try {
                return KeyStoreUtils.loadKeyStore( "JKS", stream, keyStorePassword.toCharArray() );
            }
            finally {
                Closeables.closeQuietly( stream );
            }
        }
        catch (IOException e) {
            throw Throwables.propagate( e );
        }
    }
}
