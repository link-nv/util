/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.servlet;

import net.link.util.logging.Logger;
import java.net.URI;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


/**
 * {@link HttpServletRequest} wrapper used to provide the correct endpoint URI when behind a proxy or load balancer.
 *
 * @author lhunath
 */
public class HttpServletRequestEndpointWrapper extends HttpServletRequestWrapper {

    private static final Logger logger = Logger.get( HttpServletRequestEndpointWrapper.class );

    private final URI baseURI;

    public HttpServletRequestEndpointWrapper(HttpServletRequest request, String baseURL) {

        super( request );

        baseURI = URI.create( baseURL );
    }

    @Override
    public StringBuffer getRequestURL() {

        String requestURI = getRequestURI();
        String requestURL = baseURI.resolve( requestURI ).toASCIIString();
        logger.dbg( "Resolving request URI: " + requestURI + " with base: " + baseURI + " -> " + requestURL );

        return new StringBuffer( requestURL );
    }
}
