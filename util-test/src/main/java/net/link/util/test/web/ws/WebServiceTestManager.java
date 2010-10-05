/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.test.web.ws;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Web Service Test Utils. Can be used to unit test JAX-WS endpoint implementations.
 *
 * @author fcorneli
 */
public class WebServiceTestManager {

    private static final Log LOG = LogFactory.getLog( WebServiceTestManager.class );

    private Endpoint endpoint;

    private HttpServer httpServer;

    private ExecutorService executorService;

    private int port;

    private String contextPath;

    private String servlet;

    public void setUp(Object webServicePort)
            throws Exception {

        setUp( webServicePort, "/", "test" );
    }

    public void setUp(Object webServicePort, String contextPath, String servlet)
            throws Exception {

        this.contextPath = contextPath.replaceFirst( "^/?", "/" ).replaceFirst( "/$", "" );
        this.servlet = servlet;

        endpoint = Endpoint.create( webServicePort );

        httpServer = HttpServer.create();
        port = getFreePort();
        LOG.debug( "using port: " + port );
        httpServer.bind( new InetSocketAddress( port ), 1 );
        executorService = Executors.newFixedThreadPool( 1 );
        httpServer.setExecutor( executorService );
        httpServer.start();

        String context = String.format( "%s/%s", this.contextPath, this.servlet );
        HttpContext httpContext = httpServer.createContext( context );
        endpoint.publish( httpContext );
    }

    public String getEndpointAddress() {

        return String.format( "http://localhost:%d%s/%s", port, contextPath, servlet );
    }

    public String getLocation() {

        return String.format( "http://localhost:%d%s", port, contextPath );
    }

    public void tearDown()
            throws Exception {

        endpoint.stop();
        httpServer.stop( 1 );
        executorService.shutdown();
    }

    public void setEndpointAddress(Object webServiceClientPort) {

        BindingProvider bindingProvider = (BindingProvider) webServiceClientPort;
        String endpointAddress = getEndpointAddress();
        bindingProvider.getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress );
    }

    public static int getFreePort()
            throws Exception {

        ServerSocket serverSocket = new ServerSocket( 0 );
        int port = serverSocket.getLocalPort();
        serverSocket.close();
        return port;
    }
}
