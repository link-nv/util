/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.util.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import net.link.util.test.web.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;


/**
 * Unit test for servlet test manager.
 *
 * @author fcorneli
 */
public class ServletTestManagerTest {

    static final Log LOG = LogFactory.getLog( ServletTestManagerTest.class );


    public static class TestServlet extends HttpServlet {

        private static boolean called;

        private static Map<String, String> initParams;

        private static String sessionAttributeName;

        private static String sessionAttributeValue;

        private static Map<String, Object> sessionAttributes;

        static void reset() {

            called = false;
            initParams = new HashMap<String, String>();
            sessionAttributeName = null;
            sessionAttributes = new HashMap<String, Object>();
        }

        static boolean isCalled() {

            return called;
        }

        static String getInitParam(String initParamName) {

            return initParams.get( initParamName );
        }

        @SuppressWarnings("unchecked")
        @Override
        public void init(ServletConfig config)
                throws ServletException {

            super.init( config );
            Enumeration<String> initParamNamesEnum = config.getInitParameterNames();
            while (initParamNamesEnum.hasMoreElements()) {
                String initParamName = initParamNamesEnum.nextElement();
                String initParamValue = config.getInitParameter( initParamName );
                initParams.put( initParamName, initParamValue );
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {

            LOG.debug( "doGet" );
            called = true;
            if (null != sessionAttributeName) {
                HttpSession session = request.getSession();
                LOG.debug( "setting session attribute: " + sessionAttributeName + " to " + sessionAttributeValue );
                session.setAttribute( sessionAttributeName, sessionAttributeValue );
            }
            HttpSession session = request.getSession( false );
            if (null != session) {
                LOG.debug( "session found" );
                Enumeration<String> attribNamesEnum = session.getAttributeNames();
                while (attribNamesEnum.hasMoreElements()) {
                    String attributeName = attribNamesEnum.nextElement();
                    Object attributeValue = session.getAttribute( attributeName );
                    LOG.debug( "session attribute found: " + attributeName + ", value: " + attributeValue );
                    sessionAttributes.put( attributeName, attributeValue );
                }
            }
        }

        public static void setSessionAttributeWhenInvoked(String name, String value) {

            sessionAttributeName = name;
            sessionAttributeValue = value;
        }

        public static Object getSessionAttribute(String attributeName) {

            Object attributeValue = sessionAttributes.get( attributeName );
            return attributeValue;
        }
    }

    @Test
    public void testSimpleServlet()
            throws Exception {

        ServletTestManager servletTestManager = new ServletTestManager();
        servletTestManager.setUp( new ContainerSetup( //
                new ServletSetup( TestServlet.class ) ) );
        servletTestManager.createSocketConnector();

        try {
            TestServlet.reset();
            String location = servletTestManager.getServletLocation();
            LOG.debug( "location: " + location );
            HttpClient httpClient = new HttpClient();
            GetMethod getMethod = new GetMethod( location );
            int statusCode = httpClient.executeMethod( getMethod );
            LOG.debug( "status code: " + statusCode );
            assertEquals( HttpServletResponse.SC_OK, statusCode );
            assertTrue( TestServlet.isCalled() );
        }
        finally {
            servletTestManager.tearDown();
        }
    }

    @Test
    public void testInitParams()
            throws Exception {

        ServletTestManager servletTestManager = new ServletTestManager();
        servletTestManager.setUp( new ContainerSetup( //
                new ServletSetup( TestServlet.class ) //
                        .addInitParameter( "param1", "value1" ) //
                        .addInitParameter( "param2", "value2" ) ) );
        servletTestManager.createSocketConnector();

        try {
            TestServlet.reset();
            String location = servletTestManager.getServletLocation();
            LOG.debug( "location: " + location );
            HttpClient httpClient = new HttpClient();
            GetMethod getMethod = new GetMethod( location );
            int statusCode = httpClient.executeMethod( getMethod );
            LOG.debug( "status code: " + statusCode );
            assertEquals( HttpServletResponse.SC_OK, statusCode );
            assertTrue( TestServlet.isCalled() );
            assertEquals( "value1", TestServlet.getInitParam( "param1" ) );
            assertEquals( "value2", TestServlet.getInitParam( "param2" ) );
        }
        finally {
            servletTestManager.tearDown();
        }
    }

    public static class TestFilter implements Filter {

        private static boolean called;

        private static Map<String, String> initParams;

        public static void reset() {

            called = false;
            initParams = new HashMap<String, String>();
        }

        public static boolean isCalled() {

            return called;
        }

        public static String getInitParameter(String paramName) {

            return initParams.get( paramName );
        }

        public void destroy() {

        }

        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            called = true;
            chain.doFilter( request, response );
        }

        @SuppressWarnings("unchecked")
        public void init(FilterConfig config)
                throws ServletException {

            Enumeration<String> initParamNames = config.getInitParameterNames();
            while (initParamNames.hasMoreElements()) {
                String initParamName = initParamNames.nextElement();
                LOG.debug( "filter init param: " + initParamName );
                String initParamValue = config.getInitParameter( initParamName );
                initParams.put( initParamName, initParamValue );
            }
        }
    }

    @Test
    public void testFilter()
            throws Exception {

        ServletTestManager servletTestManager = new ServletTestManager();
        TestServlet.reset();
        TestFilter.reset();
        servletTestManager.setUp( new ContainerSetup( new ServletSetup( TestServlet.class ), new FilterSetup( TestFilter.class ) ) );
        servletTestManager.createSocketConnector();

        try {
            String location = servletTestManager.getServletLocation();
            LOG.debug( "location: " + location );
            HttpClient httpClient = new HttpClient();
            GetMethod getMethod = new GetMethod( location );
            int statusCode = httpClient.executeMethod( getMethod );
            LOG.debug( "status code: " + statusCode );
            assertEquals( HttpServletResponse.SC_OK, statusCode );
            assertTrue( TestServlet.isCalled() );
            assertTrue( TestFilter.isCalled() );
        }
        finally {
            servletTestManager.tearDown();
        }
    }

    @Test
    public void testFilterInitParams()
            throws Exception {

        ServletTestManager servletTestManager = new ServletTestManager();
        TestServlet.reset();
        TestFilter.reset();
        servletTestManager.setUp( new ContainerSetup( //
                new ServletSetup( TestServlet.class ), //
                new FilterSetup( TestFilter.class ) //
                        .addInitParameter( "param1", "value1" ) //
                        .addInitParameter( "param2", "value2" ) ) );
        servletTestManager.createSocketConnector();

        try {
            String location = servletTestManager.getServletLocation();
            LOG.debug( "location: " + location );
            HttpClient httpClient = new HttpClient();
            GetMethod getMethod = new GetMethod( location );
            int statusCode = httpClient.executeMethod( getMethod );
            LOG.debug( "status code: " + statusCode );
            assertEquals( HttpServletResponse.SC_OK, statusCode );
            assertTrue( TestServlet.isCalled() );
            assertTrue( TestFilter.isCalled() );
            assertEquals( "value1", TestFilter.getInitParameter( "param1" ) );
            assertEquals( "value2", TestFilter.getInitParameter( "param2" ) );
        }
        finally {
            servletTestManager.tearDown();
        }
    }

    @Test
    public void testGetSessionAttribute()
            throws Exception {

        ServletTestManager servletTestManager = new ServletTestManager();
        TestServlet.reset();
        TestServlet.setSessionAttributeWhenInvoked( "attribute1", "value1" );
        servletTestManager.setUp( new ContainerSetup( //
                new ServletSetup( TestServlet.class ) ) );
        servletTestManager.createSocketConnector();

        try {
            String location = servletTestManager.getServletLocation();
            LOG.debug( "location: " + location );
            HttpClient httpClient = new HttpClient();
            GetMethod getMethod = new GetMethod( location );
            int statusCode = httpClient.executeMethod( getMethod );
            LOG.debug( "status code: " + statusCode );
            assertEquals( HttpServletResponse.SC_OK, statusCode );
            assertTrue( TestServlet.isCalled() );
            assertEquals( "value1", servletTestManager.getSessionAttribute( "attribute1" ) );
        }
        finally {
            servletTestManager.tearDown();
        }
    }

    @Test
    public void testSetSessionAttribute()
            throws Exception {

        ServletTestManager servletTestManager = new ServletTestManager();
        TestServlet.reset();
        servletTestManager.setUp( new ContainerSetup( //
                new ServletSetup( TestServlet.class ) ) //
                .addSessionAttribute( "attribute1", "value1" ) );
        TestServlet.setSessionAttributeWhenInvoked( "setter", "value" );
        servletTestManager.createSocketConnector();

        try {
            String location = servletTestManager.getServletLocation();
            LOG.debug( "location: " + location );
            HttpClient httpClient = new HttpClient();
            GetMethod getMethod = new GetMethod( location );
            int statusCode = httpClient.executeMethod( getMethod );
            LOG.debug( "status code: " + statusCode );
            assertEquals( HttpServletResponse.SC_OK, statusCode );
            assertTrue( TestServlet.isCalled() );
            assertEquals( "value1", TestServlet.getSessionAttribute( "attribute1" ) );
        }
        finally {
            servletTestManager.tearDown();
        }
    }
}
