package net.link.util.util;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Formatter;
import net.link.util.logging.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 12/03/14
 * Time: 13:30
 */
@SuppressWarnings("unused")
public abstract class CodeUtils {

    static final Logger logger = Logger.get( CodeUtils.class );

    public static byte[] digest(final MessageDigests digest, final String input, final Charset charset) {

        return digest( digest.get(), input, charset );
    }

    public static byte[] digest(final MessageDigest digest, final String input, final Charset charset) {

        return digest( digest, input.getBytes( charset ) );
    }

    public static byte[] digest(final MessageDigests digest, final byte[] input) {

        return digest( digest.get(), input );
    }

    public static byte[] digest(final MessageDigest digest, final byte[] input) {

        return digest.digest( input );
    }

    public static String encodeHex(@Nullable final byte[] data) {

        return encodeHex( data, false );
    }

    public static String encodeHex(@Nullable final byte[] data, final boolean pretty) {

        StringBuilder bytes = new StringBuilder( data == null? 0: (data.length + (pretty? 1: 0)) * 2 );

        Formatter formatter = null;
        try {
            formatter = new Formatter( bytes );
            String format = String.format( "%%02X%s", pretty? ":": "" );

            if (data != null)
                for (final byte b : data)
                    formatter.format( format, b );
        }
        finally {
            if (null != formatter)
                formatter.close();
        }

        if (pretty && bytes.length() > 0)
            bytes.deleteCharAt( bytes.length() - 1 );

        return bytes.toString();
    }

    public static byte[] decodeHex(@Nullable final String hexString) {

        if (hexString == null)
            return new byte[0];

        byte[] deviceToken = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2)
            deviceToken[i / 2] = Integer.valueOf( hexString.substring( i, i + 2 ), 16 ).byteValue();

        return deviceToken;
    }

    /**
     * Encode URL arguments into a URL template.  Arguments are injected after UTF-8 based URL encoding.
     *
     * @param urlFormat      The URL template to inject the arguments into.  {@code {}} gets replaced by its respective argument.
     * @param urlParam1      The first URL template argument.
     * @param otherURLParams More URL template arguments, if needed.
     *
     * @return A URL that is the result of injecting the arguments into the URL template.
     */
    public static URL encodeURL(final String urlFormat, final String urlParam1, final String... otherURLParams) {

        StringBuilder url = new StringBuilder();
        int lastURLFormatOffset = 0, urlFormatIndex = 0;
        for (int urlFormatOffset = 0; //
             (urlFormatOffset = urlFormat.indexOf( "{}", urlFormatOffset )) != -1; //
             lastURLFormatOffset = urlFormatOffset += 2, ++urlFormatIndex) {

            url.append( urlFormat.substring( lastURLFormatOffset, urlFormatOffset ) );

            if (urlFormatIndex == 0) {
                url.append( encodeURL( urlParam1 ) );
            } else {
                Preconditions.checkElementIndex( urlFormatIndex - 1, otherURLParams.length, "Not enough URL encoding parameters given." );
                url.append( encodeURL( otherURLParams[urlFormatIndex - 1] ) );
            }
        }
        if (lastURLFormatOffset != urlFormat.length())
            url.append( urlFormat.substring( lastURLFormatOffset, urlFormat.length() ) );

        try {
            return new URL( url.toString() );
        }
        catch (MalformedURLException e) {
            logger.err( e, "The URL template does not appear to specify a valid URL: %s", urlFormat );
            throw new IllegalArgumentException( e );
        }
    }

    public static String encodeURL(final String plainString) {

        return encodeURL( plainString, Charsets.UTF_8 );
    }

    private static String encodeURL(final String plainString, final Charset encoding) {

        try {
            return URLEncoder.encode( plainString, encoding.displayName() );
        }
        catch (final UnsupportedEncodingException e) {
            //noinspection ProhibitedExceptionThrown
            throw logger.bug( e, "Given encoding not supported: %s", encoding );
        }
    }

    public static String decodeBase64(@Nullable final String encoded) {

        return null != encoded? new String( Base64.decode( encoded.getBytes( Charsets.UTF_8 ) ), Charsets.UTF_8 ): null;
    }

    @Nullable
    public static byte[] decodeBase64ToBytes(@Nullable final String encoded) {

        return null != encoded? Base64.decode( encoded.getBytes( Charsets.UTF_8 ) ): null;
    }

    public static String encodeBase64(@Nullable final String content) {

        return null != content? Base64.toBase64String( content.getBytes( Charsets.UTF_8 ) ): null;
    }

    public static String encodeBase64(@Nullable final byte[] content) {

        return null != content? Base64.toBase64String( content ): null;
    }

}
