package net.link.util.ws;

import static com.google.common.base.Preconditions.checkArgument;

import com.sun.xml.ws.developer.JAXWSProperties;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.xml.ws.BindingProvider;
import net.link.util.pkix.X509CertificateTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <h2>{@link AbstractWSClient}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>10 18, 2010</i> </p>
 *
 * @author lhunath
 */
public class AbstractWSClient<P> {

    private static final Logger logger = LoggerFactory.getLogger( AbstractWSClient.class );

    private final P port;

    protected AbstractWSClient(final P port) {

        checkArgument( port instanceof BindingProvider, "Port must be a BindingProvider" );

        this.port = port;

        registerTrustManager( null );
    }

    protected P getPort() {

        return port;
    }

    protected BindingProvider getBindingProvider() {

        return (BindingProvider) port;
    }

    protected void registerTrustManager(final X509Certificate trustedCertificate) {

        logger.info( "Installing trust manager on: {}, for: {} ", getClass(), trustedCertificate );

        try {
            SSLContext sslContext = SSLContext.getInstance( "TLS" );
            sslContext.init( null, new TrustManager[] { new X509CertificateTrustManager( trustedCertificate ) }, null );

            // Setup TrustManager for validation
            getBindingProvider().getRequestContext().put( JAXWSProperties.SSL_SOCKET_FACTORY, sslContext.getSocketFactory() );
        }
        catch (KeyManagementException e) {
            throw new RuntimeException( e );
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException( e );
        }
    }
}
