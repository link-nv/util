package net.link.util.config;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.io.ByteSource;
import java.io.IOException;
import java.io.InputStream;
import net.link.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link ResourceKeyStoreKeyProvider}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>10 20, 2010</i> </p>
 *
 * @author lhunath
 */
public class ResourceKeyStoreKeyProvider extends KeyStoreKeyProvider {

    static final Logger logger = Logger.get( ResourceKeyStoreKeyProvider.class );

    public ResourceKeyStoreKeyProvider(@NotNull String keyStoreResource) {

        this( keyStoreResource, null, null, null );
    }

    public ResourceKeyStoreKeyProvider(@NotNull final String keyStoreResource, @Nullable String keyStorePassword, @Nullable String keyEntryAlias,
                                       @Nullable String keyEntryPassword) {

        super( loadKeyStore( new ByteSource() {
            @Override
            public InputStream openStream()
                    throws IOException {

                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

                return checkNotNull( classLoader.getResourceAsStream( keyStoreResource ), "Could not find keystore: %s, in classloader: %s", keyStoreResource,
                        classLoader );
            }
        }, keyStorePassword ), keyEntryAlias, keyEntryPassword );
    }
}
