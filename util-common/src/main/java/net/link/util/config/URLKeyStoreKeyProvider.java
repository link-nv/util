package net.link.util.config;

import com.google.common.io.Resources;
import java.net.URL;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link URLKeyStoreKeyProvider}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>10 20, 2010</i> </p>
 *
 * @author lhunath
 */
public class URLKeyStoreKeyProvider extends KeyStoreKeyProvider {

    public URLKeyStoreKeyProvider(@NotNull String keyStoreURL) {

        this( URLUtils.newURL( keyStoreURL ) );
    }

    public URLKeyStoreKeyProvider(@NotNull URL keyStoreURL) {

        this( keyStoreURL, null, null, null );
    }

    public URLKeyStoreKeyProvider(@NotNull URL keyStoreURL, @Nullable String keyStorePassword, @Nullable String keyEntryAlias,
                                     @Nullable String keyEntryPassword) {

        super( loadKeyStore( Resources.newInputStreamSupplier( keyStoreURL ), keyStorePassword ), keyEntryAlias, keyEntryPassword );
    }
}
