package net.link.util.util;

import com.google.common.io.ByteSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import net.link.util.logging.Logger;


/**
 * Created by wvdhaute
 * Date: 12/03/14
 * Time: 13:25
 */
public enum MessageDigests {
    MD2( "MD2" ),
    MD5( "MD5" ),
    SHA1( "SHA-1" ),
    SHA256( "SHA-256" ),
    SHA384( "SHA-384" ),
    SHA512( "SHA-512" );

    static final Logger logger = Logger.get( MessageDigests.class );

    private final String jcaName;

    MessageDigests(final String jcaName) {

        this.jcaName = jcaName;
    }

    public MessageDigest get() {

        try {
            return MessageDigest.getInstance( getJCAName() );
        }
        catch (final NoSuchAlgorithmException e) {
            throw logger.bug( e );
        }
    }

    public String getJCAName() {

        return jcaName;
    }

    public byte[] of(final byte[] bytes) {

        return of( new ByteArrayInputStream( bytes ) );
    }

    public byte[] of(final ByteSource byteSource) {

        try {
            return get().digest( byteSource.read() );
        }
        catch (final IOException e) {
            throw logger.bug( e );
        }
    }

    public byte[] of(final InputStream stream) {

        return of( stream );
    }
}
