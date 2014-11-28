package net.link.util.ws;

import static com.google.common.base.Preconditions.checkArgument;

import com.sun.xml.internal.ws.developer.JAXWSProperties;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.xml.ws.BindingProvider;
import net.link.util.InternalInconsistencyException;
import net.link.util.common.ApplicationMode;
import net.link.util.logging.Logger;
import net.link.util.ssl.X509CertificateTrustManager;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link AbstractWSClient}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>10 18, 2010</i> </p>
 *
 * @author lhunath
 */
public class AbstractWSClient<P> {

    private static final Logger logger = Logger.get( AbstractWSClient.class );

    public static final String JAX_WS_RI_HOSTNAME_VERIFIER  = "com.sun.xml.ws.transport.https.client.hostname.verifier";
    public static final String JAX_WS_RI_SSL_SOCKET_FACTORY = "com.sun.xml.ws.transport.https.client.SSLSocketFactory";

    private final P port;

    protected AbstractWSClient(final P port) {

        this( port, null );
    }

    protected AbstractWSClient(final P port, @Nullable final X509Certificate[] sslCertificates) {

        checkArgument( port instanceof BindingProvider, "Port must be a BindingProvider" );

        this.port = port;

        registerTrustManager( sslCertificates );
    }

    protected P getPort() {

        return port;
    }

    protected BindingProvider getBindingProvider() {

        return (BindingProvider) port;
    }

    protected void registerTrustManager(final X509Certificate[] trustedCertificates) {

        if (null == trustedCertificates) {
            return;
        }

        logger.dbg( "Installing trust manager on: {}, for: {} ", getClass(), Arrays.asList( trustedCertificates ) );

        try {
            SSLContext sslContext = SSLContext.getInstance( "TLS" );
            sslContext.init( null, new TrustManager[] { new X509CertificateTrustManager( trustedCertificates ) }, null );

            // Setup TrustManager for validation
            getBindingProvider().getRequestContext().put( JAXWSProperties.SSL_SOCKET_FACTORY, sslContext.getSocketFactory() );
            getBindingProvider().getRequestContext().put( JAX_WS_RI_SSL_SOCKET_FACTORY, sslContext.getSocketFactory() );

            // skip hostname validation for SSL in debug/demo mode ...
            if (ApplicationMode.get() != ApplicationMode.DEPLOYMENT) {
                getBindingProvider().getRequestContext().put( JAXWSProperties.HOSTNAME_VERIFIER, new SkipHostnameVerifier() );
                getBindingProvider().getRequestContext().put( JAX_WS_RI_HOSTNAME_VERIFIER, new SkipHostnameVerifier() );
            }
        }
        catch (KeyManagementException e) {
            throw new InternalInconsistencyException( e );
        }
        catch (NoSuchAlgorithmException e) {
            throw new InternalInconsistencyException( e );
        }
    }

    static class SkipHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(final String s, final SSLSession sslSession) {

            return true;
        }
    }
}
