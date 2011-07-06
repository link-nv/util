package net.link.util.config;

import com.google.common.io.Files;
import java.io.File;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link FileKeyStoreKeyProvider}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>10 20, 2010</i> </p>
 *
 * @author lhunath
 */
public class FileKeyStoreKeyProvider extends KeyStoreKeyProvider {

    public FileKeyStoreKeyProvider(String keyStoreFile) {

        this( new File( keyStoreFile ) );
    }

    public FileKeyStoreKeyProvider(@NotNull File keyStoreFile) {

        this( keyStoreFile, null, null, null );
    }

    public FileKeyStoreKeyProvider(@NotNull File keyStoreFile, @Nullable String keyStorePassword, @Nullable String keyEntryAlias,
                                      @Nullable String keyEntryPassword) {

        super( loadKeyStore( Files.newInputStreamSupplier( keyStoreFile ), keyStorePassword ), keyEntryAlias, keyEntryPassword );
    }
}
