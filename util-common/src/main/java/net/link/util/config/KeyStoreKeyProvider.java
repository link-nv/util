package net.link.util.config;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import net.link.util.common.KeyUtils;
import net.link.util.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link KeyStoreKeyProvider}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>10 20, 2010</i> </p>
 *
 * @author lhunath
 */
public class KeyStoreKeyProvider extends KeyProviderImpl {

    public static final String IDENTITY_ALIAS = "identity";

    public KeyStoreKeyProvider(@NotNull KeyStore keyStore) {

        this( keyStore, null, null );
    }

    public KeyStoreKeyProvider(@NotNull KeyStore keyStore, @Nullable String keyEntryAlias, @Nullable String keyEntryPassword) {

        super( getIdentity( keyStore, keyEntryAlias, keyEntryPassword ), KeyUtils.getCertificates( keyStore, null ) );
    }

    private static KeyStore.PrivateKeyEntry getIdentity(final KeyStore keyStore, final String keyEntryAlias, final String keyEntryPassword) {

        try {
            String alias = ObjectUtils.ifNotNullElse( keyEntryAlias, IDENTITY_ALIAS );
            KeyStore.Entry entry = keyStore.getEntry( alias, new KeyStore.PasswordProtection( keyEntryPassword.toCharArray() ) );

            checkNotNull( entry, "Identity entry (alias: %s) missing from the key store.  Known entries: ", alias,
                    ImmutableList.copyOf( Iterators.forEnumeration( keyStore.aliases() ) ) );
            checkState( entry instanceof KeyStore.PrivateKeyEntry, "Identity entry (alias: %s) in the key store should be a private key.  Found a: %s", alias,
                    entry.getClass() );

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
                return KeyUtils.loadKeyStore( "JKS", checkNotNull( stream, "Keystore input stream cannot be null." ),
                        null != keyStorePassword? keyStorePassword.toCharArray(): null );
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
