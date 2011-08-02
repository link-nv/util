/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import net.link.util.servlet.annotation.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Abstract Injection Servlet.
 * <p/>
 * <ul>
 * <li>Injects request parameters into servlet fields.
 * <li>Injects and outjects session parameters.
 * <li>Injects EJBs.
 * <li>Injects servlet init parameters. If no defaultValue is specified, an {@link UnavailableException} will be thrown.
 * <li>Injects servlet context parameters. If no defaultValue is specified, an {@link UnavailableException} will be thrown.
 * <li>By default checks if the servlet is accessed with a secure connection. If context parameter <code>Protocol</code> is
 * <code>http</code> or <code>securityCheck</code> is set to <code>false</code> this check will be ommitted.
 * </ul>
 *
 * @author fcorneli
 */
public abstract class AbstractInjectionServlet extends HttpServlet {

    static final Log LOG = LogFactory.getLog( AbstractInjectionServlet.class );

    protected Map<String, String> configParams;

    @Override
    public void init(ServletConfig config)
            throws ServletException {

        super.init( config );

        initInitParameters( config );
        initContextParameters( config );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // force UTF-8 encoding
        try {
            request.setCharacterEncoding( "UTF8" );
            response.setCharacterEncoding( "UTF8" );
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException( e );
        }

        LOG.debug( "servlet " + getClass() + " beginning service" );
        String endpoint = getWrapperEndpoint( request );
        if (endpoint != null) {
            HttpServletRequestEndpointWrapper wrappedRequest = new HttpServletRequestEndpointWrapper( request, endpoint );
            HttpServletResponseEndpointWrapper wrappedResponse = new HttpServletResponseEndpointWrapper( response, endpoint );

            LOG.debug( "Wrapped request and response using endpoint: " + endpoint );
            super.service( wrappedRequest, wrappedResponse );
        } else {
            LOG.debug( "No endpoint defined.  Not wrapping request and response." );
            super.service( request, response );
        }
    }

    /**
     * <b>REQUEST AND RESPONSE WRAPPING</b>:
     * <p/>
     * <p>
     * When we're behind a proxy or load balancer, the servlet request URI that the container gives us points to this machine rather than
     * the server that the request was actually sent to. This causes validation issues in OpenSAML and problems when redirecting to
     * relative
     * URIs.
     * </p>
     * <p/>
     * <p>
     * <code>[User] >--[ https://linkid.be/app/foo ]--> [Proxy] >--[ http://provider.com/linkid/app/foo ]--> [linkID]</code> <br>
     * <code>[linkID: redirect to bar] >--[ redirect: http://provider.com/linkid/app/bar ]--> [Proxy] >--[ redirect:
     * http://provider.com/linkid/app/bar]--> [User]</code>
     * <br>
     * <code>[User] >--[ http://provider.com/linkid/app/bar ]--> [Problem!]</code>
     * </p>
     * <p/>
     * <p>
     * To solve this problem, we wrap the servlet request and response such that the request URI in the HttpServletRequest is the request
     * URI of the client's request (the request to the proxy/load balancer), and such that sendRedirects with relative URIs are translated
     * to absolute URIs using the client's request URI base.
     * </p>
     *
     * @return The endpoint URL that the wrapper should use to replace the servlet request's requestURI and to calculate the absolute URL
     *         for the servlet response's relative sendRedirects.
     */
    protected abstract String getWrapperEndpoint(HttpServletRequest request);

    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        doGetInvocation( request, response );
    }

    @Override
    protected final void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        doPostInvocation( request, response );
    }

    protected void doGetInvocation(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        injectRequestParameters( request );
        injectSessionAttributes( session );
        InjectionResponseWrapper responseWrapper = new InjectionResponseWrapper( response );
        invokeGet( request, responseWrapper );
        outjectSessionAttributes( session );
        responseWrapper.commit();
    }

    protected void doPostInvocation(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        injectRequestParameters( request );
        injectSessionAttributes( session );
        InjectionResponseWrapper responseWrapper = new InjectionResponseWrapper( response );
        invokePost( request, responseWrapper );
        outjectSessionAttributes( session );
        responseWrapper.commit();
    }

    /**
     * Invalidate the old session, start a new one, and repeat the injection process.
     *
     * @return The new session (Also available via {@link HttpServletRequest#getSession(boolean)}, of course).
     */
    protected HttpSession restartSession(HttpServletRequest request)
            throws ServletException {

        HttpSession session = request.getSession( false );
        if (session != null)
            session.invalidate();
        session = request.getSession( true );

        injectRequestParameters( request );
        injectSessionAttributes( session );

        return session;
    }

    /**
     * Injection response wrapper. We use a response wrapper since we want to be able to postpone some actions.
     *
     * @author fcorneli
     */
    public static class InjectionResponseWrapper extends HttpServletResponseWrapper {

        private String redirectLocation;

        public InjectionResponseWrapper(HttpServletResponse response) {

            super( response );
        }

        @Override
        public void sendRedirect(String location)
                throws IOException {

            if (null != redirectLocation)
                throw new IllegalStateException( "cannot send redirect twice" );

            redirectLocation = location;
        }

        public void commit()
                throws IOException {

            if (null != redirectLocation)
                super.sendRedirect( redirectLocation );
        }
    }

    protected void invokeGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        super.doGet( request, response );
    }

    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        super.doPost( request, response );
    }

    private void injectRequestParameters(HttpServletRequest request)
            throws ServletException {

        for (Class<?> type = getClass(); type != Object.class; type = type.getSuperclass()) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                RequestParameter requestParameterAnnotation = field.getAnnotation( RequestParameter.class );
                if (null == requestParameterAnnotation)
                    continue;
                String requestParameterName = requestParameterAnnotation.value();
                String value = request.getParameter( requestParameterName );
                if (null == value)
                    continue;
                field.setAccessible( true );
                try {
                    field.set( this, value );
                }
                catch (IllegalArgumentException e) {
                    throw new ServletException( "illegal argument: " + e.getMessage(), e );
                }
                catch (IllegalAccessException e) {
                    throw new ServletException( "illegal access: " + e.getMessage(), e );
                }
            }
        }
    }

    private void injectSessionAttributes(HttpSession session)
            throws ServletException {

        for (Class<?> type = getClass(); type != Object.class; type = type.getSuperclass()) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                In inAnnotation = field.getAnnotation( In.class );
                if (null == inAnnotation)
                    continue;
                String inName = inAnnotation.value();
                Object value = session.getAttribute( inName );
                if (inAnnotation.required())
                    if (null == value)
                        throw new ServletException( "missing required session attribute: " + inName );
                field.setAccessible( true );
                try {
                    field.set( this, value );
                }
                catch (IllegalArgumentException e) {
                    throw new ServletException( "illegal argument: " + e.getMessage(), e );
                }
                catch (IllegalAccessException e) {
                    throw new ServletException( "illegal access: " + e.getMessage(), e );
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void initInitParameters(ServletConfig config)
            throws ServletException {

        for (Class<?> type = getClass(); type != Object.class; type = type.getSuperclass()) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                Init initAnnotation = field.getAnnotation( Init.class );
                if (null == initAnnotation)
                    continue;
                String name = initAnnotation.name();
                if (null == name)
                    throw new ServletException( "@Init name attribute required" );
                LOG.debug( "init: " + name );
                String defaultValue = initAnnotation.defaultValue();
                boolean optional = initAnnotation.optional();
                boolean checkContext = initAnnotation.checkContext();

                String value = config.getInitParameter( name );
                if (null == value && checkContext)
                    value = config.getServletContext().getInitParameter( name );
                if (null == value) {
                    if (Init.NOT_SPECIFIED.equals( defaultValue ) && !optional)
                        throw new UnavailableException( "missing init parameter: " + name );
                    if (Init.NOT_SPECIFIED.equals( defaultValue ))
                        defaultValue = null;
                    value = defaultValue;
                }
                field.setAccessible( true );
                try {
                    field.set( this, value );
                }
                catch (IllegalArgumentException e) {
                    throw new ServletException( "illegal argument: " + e.getMessage(), e );
                }
                catch (IllegalAccessException e) {
                    throw new ServletException( "illegal access: " + e.getMessage(), e );
                }
            }
            configParams = new HashMap<String, String>();
            Enumeration<String> initParamsEnum = config.getInitParameterNames();
            while (initParamsEnum.hasMoreElements()) {
                String paramName = initParamsEnum.nextElement();
                String paramValue = config.getInitParameter( paramName );
                LOG.debug( "config param: " + paramName + "=" + paramValue );
                configParams.put( paramName, paramValue );
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void initContextParameters(ServletConfig config)
            throws ServletException {

        for (Class<?> type = getClass(); type != Object.class; type = type.getSuperclass()) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                Context contextAnnotation = field.getAnnotation( Context.class );
                if (null == contextAnnotation)
                    continue;
                String name = contextAnnotation.name();
                if (null == name)
                    throw new ServletException( "@Context name attribute required" );
                LOG.debug( "init: " + name );
                String defaultValue = contextAnnotation.defaultValue();
                boolean optional = contextAnnotation.optional();

                String value = config.getServletContext().getInitParameter( name );
                if (null == value) {
                    if (Context.NOT_SPECIFIED.equals( defaultValue ) && !optional)
                        throw new UnavailableException( "missing init parameter: " + name );
                    if (Context.NOT_SPECIFIED.equals( defaultValue ))
                        defaultValue = null;
                    value = defaultValue;
                }
                field.setAccessible( true );
                try {
                    field.set( this, value );
                }
                catch (IllegalArgumentException e) {
                    throw new ServletException( "illegal argument: " + e.getMessage(), e );
                }
                catch (IllegalAccessException e) {
                    throw new ServletException( "illegal access: " + e.getMessage(), e );
                }
            }
            Enumeration<String> initParamsEnum = config.getServletContext().getInitParameterNames();
            while (initParamsEnum.hasMoreElements()) {
                String paramName = initParamsEnum.nextElement();
                if (null == configParams.get( paramName )) {
                    String paramValue = config.getServletContext().getInitParameter( paramName );
                    LOG.debug( "config param: " + paramName + "=" + paramValue );
                    configParams.put( paramName, paramValue );
                }
            }
        }
    }

    private void outjectSessionAttributes(HttpSession session)
            throws ServletException {

        for (Class<?> type = getClass(); type != Object.class; type = type.getSuperclass()) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                Out outAnnotation = field.getAnnotation( Out.class );
                if (null == outAnnotation)
                    continue;
                String outName = outAnnotation.value();
                field.setAccessible( true );
                Object value;
                try {
                    value = field.get( this );
                    if (value == null && outAnnotation.required())
                        throw new ServletException( "missing required session attribute: " + outName );
                }
                catch (IllegalArgumentException e) {
                    throw new ServletException( "illegal argument: " + e.getMessage(), e );
                }
                catch (IllegalAccessException e) {
                    throw new ServletException( "illegal access: " + e.getMessage(), e );
                }
                LOG.debug( "outjecting to session attribute: " + outName );
                session.setAttribute( outName, value );
            }
        }
    }
}
