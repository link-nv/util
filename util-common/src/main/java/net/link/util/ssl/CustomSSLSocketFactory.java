package net.link.util.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.*;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@SuppressWarnings("UnusedDeclaration")
public class CustomSSLSocketFactory extends SSLSocketFactory {

    private static final Log LOG = LogFactory.getLog( CustomSSLSocketFactory.class );

    private final SSLContext sslContext;

    /**
     * Trusts all server certificates.
     *
     * @throws NoSuchAlgorithmException could not get an SSLContext instance
     * @throws KeyManagementException   failed to initialize the SSLContext
     */
    public CustomSSLSocketFactory()
            throws NoSuchAlgorithmException, KeyManagementException {

        sslContext = SSLContext.getInstance( "SSL" );
        TrustManager trustManager = new X509CertificateTrustManager();
        TrustManager[] trustManagers = { trustManager };
        sslContext.init( null, trustManagers, null );
    }

    /**
     * Trust only the given server certificate, and the default trusted server certificates.
     *
     * @param serverCertificate SSL certificate to trust
     *
     * @throws NoSuchAlgorithmException could not get an SSLContext instance
     * @throws KeyManagementException   failed to initialize the SSLContext
     * @throws KeyStoreException        failed to intialize the {@link X509CertificateTrustManager}
     */
    public CustomSSLSocketFactory(X509Certificate serverCertificate)
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {

        sslContext = SSLContext.getInstance( "SSL" );
        TrustManager trustManager = new X509CertificateTrustManager( serverCertificate );
        TrustManager[] trustManagers = { trustManager };
        sslContext.init( null, trustManagers, null );
    }

    @Override
    public Socket createSocket()
            throws IOException {

        return sslContext.getSocketFactory().createSocket();
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort)
            throws IOException {

        return sslContext.getSocketFactory().createSocket( host, port, clientHost, clientPort );
    }

    @Override
    public Socket createSocket(String host, int port)
            throws IOException {

        return sslContext.getSocketFactory().createSocket( host, port );
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
            throws IOException {

        return sslContext.getSocketFactory().createSocket( socket, host, port, autoClose );
    }

    @Override
    public String[] getDefaultCipherSuites() {

        return sslContext.getSocketFactory().getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {

        return sslContext.getSocketFactory().getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(InetAddress host, int port)
            throws IOException {

        return sslContext.getSocketFactory().createSocket( host, port );
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
            throws IOException {

        return sslContext.getSocketFactory().createSocket( address, port, localAddress, localPort );
    }

    /**
     * Install the OpenID SSL Socket Factory. Trusts the given server certificate and all default trusted server certificates.
     *
     * @param serverCertificate SSL Certificate to trust
     *
     * @throws NoSuchAlgorithmException could not get an SSLContext instance
     * @throws KeyManagementException   failed to initialize the SSLContext
     * @throws KeyStoreException        failed to intialize the {@link X509CertificateTrustManager}
     */
    public static void install(X509Certificate serverCertificate)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        SSLSocketFactory sslSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        if (!(sslSocketFactory instanceof CustomSSLSocketFactory)) {
            LOG.debug( "installing OpenID SSL Socket Factory..." );
            CustomSSLSocketFactory openIDSSLSocketFactory = new CustomSSLSocketFactory( serverCertificate );
            HttpsURLConnection.setDefaultSSLSocketFactory( openIDSSLSocketFactory );
        } else {
            LOG.debug( "OpenID SSL Socket Factory already installed." );
        }
    }

    /**
     * Installs the OpenID SSL Socket Factory. Trusts all server certificates. For testing purposes only!
     *
     * @throws NoSuchAlgorithmException could not get an SSLContext instance
     * @throws KeyManagementException   failed to initialize the SSLContext
     */
    public static void installAllTrusted()
            throws KeyManagementException, NoSuchAlgorithmException {

        SSLSocketFactory sslSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        if (!(sslSocketFactory instanceof CustomSSLSocketFactory)) {
            LOG.debug( "installing OpenID SSL Socket Factory..." );
            CustomSSLSocketFactory openIDSSLSocketFactory = new CustomSSLSocketFactory();
            HttpsURLConnection.setDefaultSSLSocketFactory( openIDSSLSocketFactory );
            System.setProperty( "java.protocol.handler.pkgs", "javax.net.ssl" );
            HttpsURLConnection.setDefaultHostnameVerifier( org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER );
        } else {
            LOG.debug( "OpenID SSL Socket Factory already installed." );
        }
    }
}
