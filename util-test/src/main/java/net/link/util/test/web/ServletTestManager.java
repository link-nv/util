/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.test.web;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.LocalConnector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.*;


/**
 * Servlet Test Manager. This test manager allows one to unit test servlets. It is using the embeddable Jetty servlet container.
 *
 * @author fcorneli
 */
@SuppressWarnings( { "ProhibitedExceptionDeclared" })
public class ServletTestManager {

    static final Log LOG = LogFactory.getLog( ServletTestManager.class );

    private Server server;

    private String         baseURL;
    private String         contextPath;
    private ServletContext servletContext;
    TestHashSessionManager sessionManager;


    public static class TestHashSessionManager extends HashSessionManager {

        final Map<String, Serializable> initialSessionAttributes;

        public TestHashSessionManager() {

            initialSessionAttributes = new HashMap<String, Serializable>();
        }

        public void setInitialSessionAttribute(String name, Serializable value) {

            initialSessionAttributes.put( name, value );
        }

        @Override
        protected Session newSession(HttpServletRequest request) {

            LOG.debug( "new session" );
            Session session = (Session) super.newSession( request );
            for (Map.Entry<String, Serializable> mapEntry : initialSessionAttributes.entrySet()) {
                LOG.debug( "setting attribute: " + mapEntry.getKey() );
                session.setAttribute( mapEntry.getKey(), mapEntry.getValue() );
            }
            return session;
        }
    }

    public ServletContext setUp(ContainerSetup setup)
            throws Exception {

        Context context = new Context( null, new SessionHandler( sessionManager = new TestHashSessionManager() ), new SecurityHandler(),
                                       null, null );
        sessionManager.initialSessionAttributes.putAll( setup.getSessionAttributes() );
        context.setContextPath( contextPath = setup.getContextPath() );
        context.setInitParams( setup.getContextParameters() );

        for (FilterSetup filterSetup : setup.getFilters())
            context.addFilter( filterSetup.getType(), contextPath, Handler.DEFAULT ).setInitParameters( filterSetup.getInitParameters() );

        ServletHolder servletHolder = new ServletHolder();
        servletHolder.setClassName( setup.getServlet().getType().getName() );
        String servletName = setup.getServlet().getType().getSimpleName();
        if (servletName == null)
            servletName = setup.getServlet().getType().getName();
        servletHolder.setName( servletName );
        servletHolder.setInitParameters( setup.getServlet().getInitParameters() );

        ServletMapping servletMapping = new ServletMapping();
        servletMapping.setServletName( servletName );
        servletMapping.setPathSpecs( new String[] { "/*", contextPath } );

        ServletHandler handler = context.getServletHandler();
        handler.addServlet( servletHolder );
        handler.addServletMapping( servletMapping );

        server = new Server();
        server.addConnector( new LocalConnector() );
        server.addHandler( context );
        server.start();

        return servletContext = handler.getServletContext();
    }

    /**
     * Create a connector for the servlet manager.
     *
     * @return The base URL where the connector deploys its servlets.
     *
     * @throws Exception something went wrong...
     */
    public String createSocketConnector()
            throws Exception {

        if (baseURL != null)
            throw new IllegalStateException( "A connector has already been created at: " + baseURL );

        SocketConnector connector = new SocketConnector();
        connector.setHost( "127.0.0.1" );
        server.addConnector( connector );
        if (server.isStarted())
            connector.start();
        else
            connector.open();

        return baseURL = String.format( "http://%s:%d", connector.getHost(), connector.getLocalPort() );
    }

    public ServletContext getServletContext() {

        return servletContext;
    }

    public String getServletLocation() {

        return checkNotNull( baseURL, "Connector has not yet been created." ) + contextPath;
    }

    public void tearDown()
            throws Exception {

        server.stop();
    }

    @SuppressWarnings( { "unchecked" })
    public Object getSessionAttribute(String name) {

        Map<String, AbstractSessionManager.Session> sessions = sessionManager.getSessionMap();
        AbstractSessionManager.Session session = sessions.values().iterator().next();
        String sessionId = session.getId();
        LOG.debug( "session id: " + sessionId );
        return session.getAttribute( name );
    }

    /**
     * We update all existing sessions + we make sure that new session also get this session attribute.
     *
     * @param name  session attribtue name
     * @param value session attribute value
     */
    @SuppressWarnings( { "unchecked" })
    public void setSessionAttribute(String name, Serializable value) {

        Map<String, AbstractSessionManager.Session> sessions = sessionManager.getSessionMap();
        for (AbstractSessionManager.Session session : sessions.values())
            session.setAttribute( name, value );
        sessionManager.setInitialSessionAttribute( name, value );
    }
}
