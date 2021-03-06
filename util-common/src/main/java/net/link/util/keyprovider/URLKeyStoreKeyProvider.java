package net.link.util.keyprovider;

import com.google.common.io.Resources;
import java.net.URL;
import net.link.util.common.URLUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link URLKeyStoreKeyProvider}<br> <sub>[in short].</sub></h2>
 * <p/>
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

        super( loadKeyStore( Resources.asByteSource( keyStoreURL ), keyStorePassword ), keyEntryAlias, keyEntryPassword );
    }
}
