/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.test.web;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Connector;
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
public class ServletTestManager {

    static final Log LOG = LogFactory.getLog( ServletTestManager.class );

    private Server server;

    private String contextPath;


    public static class TestHashSessionManager extends HashSessionManager {

        final Map<String, Object> initialSessionAttributes;


        public TestHashSessionManager() {

            initialSessionAttributes = new HashMap<String, Object>();
        }

        public void setInitialSessionAttribute(String name, Object value) {

            initialSessionAttributes.put( name, value );
        }

        @Override
        protected Session newSession(HttpServletRequest request) {

            LOG.debug( "new session" );
            Session session = (Session) super.newSession( request );
            for (Map.Entry<String, Object> mapEntry : initialSessionAttributes.entrySet()) {
                LOG.debug( "setting attribute: " + mapEntry.getKey() );
                session.setAttribute( mapEntry.getKey(), mapEntry.getValue() );
            }
            return session;
        }
    }


    public void setUp(Class<?> servletClass)
            throws Exception {

        setUp( servletClass, null, null, null );
    }

    public void setUp(Class<?> servletClass, Map<String, String> servletInitParameters)
            throws Exception {

        setUp( servletClass, servletInitParameters, null, null, null );
    }


    TestHashSessionManager sessionManager;


    public void setUp(Class<?> servletClass, Class<?> filterClass)
            throws Exception {

        setUp( servletClass, null, filterClass, null, null );
    }

    public void setUp(Class<?> servletClass, Class<?> filterClass, Map<String, String> filterInitParameters)
            throws Exception {

        setUp( servletClass, null, filterClass, filterInitParameters, null );
    }

    public void setUp(Class<?> servletClass, Class<?> filterClass, Map<String, String> filterInitParameters,
                      Map<String, Object> initialSessionAttributes)
            throws Exception {

        setUp( servletClass, null, filterClass, filterInitParameters, initialSessionAttributes );
    }

    public void setUp(Class<?> servletClass, Map<String, String> contextInitParameters, Class<?> filterClass,
                      Map<String, String> filterInitParameters, Map<String, Object> initialSessionAttributes)
            throws Exception {

        setUp( servletClass, "/", contextInitParameters, filterClass, filterInitParameters, initialSessionAttributes );
    }

    public void setUp(Class<?> servletClass, String contextPath, Map<String, String> contextInitParameters, Class<?> filterClass,
                      Map<String, String> filterInitParameters, Map<String, Object> initialSessionAttributes)
            throws Exception {

        server = new Server();
        this.contextPath = contextPath;

        Connector connector = new LocalConnector();
        sessionManager = new TestHashSessionManager();
        Context context = new Context( null, new SessionHandler( sessionManager ), new SecurityHandler(), null, null );
        context.setContextPath( contextPath );
        server.addConnector( connector );
        server.addHandler( context );

        if (null != contextInitParameters)
            context.setInitParams( contextInitParameters );

        if (null != filterClass) {
            FilterHolder filterHolder = context.addFilter( filterClass, this.contextPath, Handler.DEFAULT );
            if (null != filterInitParameters)
                filterHolder.setInitParameters( filterInitParameters );
        }

        ServletHandler handler = context.getServletHandler();

        ServletHolder servletHolder = new ServletHolder();
        String servletClassName = servletClass.getName();
        servletHolder.setClassName( servletClassName );
        String servletName = "TestServlet";
        servletHolder.setName( servletName );
        if (null != contextInitParameters)
            servletHolder.setInitParameters( contextInitParameters );
        handler.addServlet( servletHolder );

        ServletMapping servletMapping = new ServletMapping();
        servletMapping.setServletName( servletName );
        servletMapping.setPathSpecs( new String[] { "/*", this.contextPath } );
        handler.addServletMapping( servletMapping );

        server.start();

        if (null != initialSessionAttributes)
            sessionManager.initialSessionAttributes.putAll( initialSessionAttributes );
    }

    private String createSocketConnector()
            throws Exception {

        SocketConnector connector = new SocketConnector();
        connector.setHost( "127.0.0.1" );
        server.addConnector( connector );
        if (server.isStarted())
            connector.start();
        else
            connector.open();

        return "http://127.0.0.1:" + connector.getLocalPort();
    }

    public String getServletLocation()
            throws Exception {

        return createSocketConnector() + contextPath;
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
        Object value = session.getAttribute( name );
        return value;
    }

    /**
     * We update all existing sessions + we make sure that new session also get this session attribute.
     */
    @SuppressWarnings( { "unchecked" })
    public void setSessionAttribute(String name, Object value) {

        Map<String, AbstractSessionManager.Session> sessions = sessionManager.getSessionMap();
        for (AbstractSessionManager.Session session : sessions.values())
            session.setAttribute( name, value );
        sessionManager.setInitialSessionAttribute( name, value );
    }
}
