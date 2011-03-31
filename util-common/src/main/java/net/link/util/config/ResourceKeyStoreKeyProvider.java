package net.link.util.config;

import com.google.common.io.InputSupplier;
import java.io.IOException;
import java.io.InputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link ResourceKeyStoreKeyProvider}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>10 20, 2010</i> </p>
 *
 * @author lhunath
 */
public class ResourceKeyStoreKeyProvider extends KeyStoreKeyProvider {

    public ResourceKeyStoreKeyProvider(@NotNull String keyStoreResource) {

        this( keyStoreResource, null, null, null );
    }

    protected ResourceKeyStoreKeyProvider(@NotNull final String keyStoreResource, @Nullable String keyStorePassword,
                                          @Nullable String keyEntryAlias, @Nullable String keyEntryPassword) {

        super( loadKeyStore( new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput()
                    throws IOException {

                return Thread.currentThread().getContextClassLoader().getResourceAsStream( keyStoreResource );
            }
        }, keyStorePassword ), keyEntryAlias, keyEntryPassword );
    }
}
