/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.servlet;

import net.link.util.logging.Logger;
import java.lang.reflect.Field;
import java.util.*;
import javax.ejb.EJB;
import javax.servlet.*;
import javax.servlet.http.*;
import net.link.util.j2ee.EJBUtils;
import net.link.util.j2ee.FieldNamingStrategy;
import net.link.util.servlet.annotation.Context;
import net.link.util.servlet.annotation.Init;


/**
 * Abstract Injection Filter.
 * <p/>
 * <ul>
 * <li>Injects EJBs.
 * <li>Injects filter init parameters. If no defaultValue is specified, an {@link UnavailableException} will be thrown.
 * <li>Injects filter context parameters. If no defaultValue is specified, an {@link UnavailableException} will be thrown.
 * </ul>
 *
 * @author wvdhaute
 */
public abstract class AbstractInjectionFilter implements Filter {

    private static final Logger logger = Logger.get( AbstractInjectionFilter.class );

    protected Map<String, String> configParams;

    public void init(FilterConfig config)
            throws ServletException {

        initInitParameters( config );
        initContextParameters( config );
        injectEjbs();
    }

    private void injectEjbs()
            throws ServletException {

        for (Class<?> type = getClass(); type != Object.class; type = type.getSuperclass()) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                EJB ejb = field.getAnnotation( EJB.class );
                if (null == ejb)
                    continue;
                String mappedName = ejb.mappedName();
                Class<?> fieldType = field.getType();
                if (false == fieldType.isInterface())
                    throw new ServletException( String.format( "field %s.%s's type should be an interface", getClass(), field ) );
                if (mappedName == null || mappedName.length() == 0)
                    mappedName = new FieldNamingStrategy().calculateName( fieldType );
                if (mappedName == null || mappedName.length() == 0)
                    throw new ServletException( String.format( "field %s.%s's @EJB requires mappedName attribute", getClass(), field ) );
                logger.dbg( "injecting: " + mappedName );

                try {
                    Object ejbRef = EJBUtils.getEJB( mappedName, fieldType );
                    field.setAccessible( true );
                    try {
                        field.set( this, ejbRef );
                    }
                    catch (IllegalArgumentException e) {
                        throw new ServletException( String.format( "while injecting into %s:", getClass() ), e );
                    }
                    catch (IllegalAccessException e) {
                        throw new ServletException( String.format( "while injecting into %s:", getClass() ), e );
                    }
                }
                catch (RuntimeException e) {
                    throw new ServletException( String.format( "couldn't resolve EJB named: %s (while injecting into %s)", mappedName, getClass() ), e );
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void initInitParameters(FilterConfig config)
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
                logger.dbg( "init: " + name );
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
                logger.dbg( "config param: " + paramName + "=" + paramValue );
                configParams.put( paramName, paramValue );
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void initContextParameters(FilterConfig config)
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
                logger.dbg( "init: " + name );
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
                    logger.dbg( "config param: " + paramName + "=" + paramValue );
                    configParams.put( paramName, paramValue );
                }
            }
        }
    }

    protected static void addCookie(String name, String value, String path, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        if (true == hasCookie( name, httpRequest ))
            return;
        Cookie cookie = new Cookie( name, value );
        cookie.setPath( path );
        logger.dbg( "adding cookie: " + name + "=" + value + " path=" + cookie.getPath() );
        httpResponse.addCookie( cookie );
    }

    protected static void setCookie(String name, String value, String path, HttpServletResponse httpResponse) {

        Cookie cookie = new Cookie( name, value );
        cookie.setPath( path );
        logger.dbg( "setting cookie: " + name + "=" + value );
        httpResponse.addCookie( cookie );
    }

    protected static void removeCookie(String name, String path, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        if (false == hasCookie( name, httpRequest ))
            return;
        logger.dbg( "removing cookie: " + name );
        Cookie cookie = new Cookie( name, "" );
        cookie.setPath( path );
        cookie.setMaxAge( 0 );
        httpResponse.addCookie( cookie );
    }

    protected static boolean hasCookie(String name, HttpServletRequest httpRequest) {

        Cookie[] cookies = httpRequest.getCookies();
        if (null == cookies)
            return false;
        for (Cookie cookie : cookies)
            if (name.equals( cookie.getName() ))
                return true;
        return false;
    }

    protected static String findCookieValue(String name, HttpServletRequest httpRequest) {

        Cookie[] cookies = httpRequest.getCookies();
        if (null == cookies)
            return null;
        for (Cookie cookie : cookies)
            if (name.equals( cookie.getName() ))
                return cookie.getValue();
        return null;
    }
}
