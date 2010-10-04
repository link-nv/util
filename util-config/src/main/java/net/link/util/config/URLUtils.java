/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.config;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


/**
 * <h2>{@link URLUtils}<br>
 * <sub>Utilities for working with URLs.</sub></h2>
 *
 * <p>
 * <i>Sep 17, 2009</i>
 * </p>
 *
 * @author lhunath
 */
public abstract class URLUtils {

    /**
     * Add a GET parameter to the query component of the given URL.
     *
     * @param url The base URL on which to append the parameter.
     * @param key The key of the parameter to add.
     * @param value The value of the parameter to add.
     *
     * @return A new URL which is the base URL with the given query parameter added to it.
     */
    public static URL addParameter(URL url, String key, Object value) {

        try {
            return new URL( addParameter( url.toExternalForm(), key, value ) );
        } catch (MalformedURLException e) {
            throw new IllegalStateException( "Bug.", e );
        }
    }

    /**
     * Add a GET parameter to the query component of the given URL.
     *
     * @param url The base URL on which to append the parameter.
     * @param key The key of the parameter to add.
     * @param value The value of the parameter to add.
     *
     * @return A new URL which is the base URL with the given query parameter added to it.
     */
    public static String addParameter(String url, String key, Object value) {

        if (key == null)
            throw new IllegalArgumentException( "key to add to url can't be null" );

        StringBuffer urlString = new StringBuffer( url );
        if (!url.contains( "?" ))
            urlString.append( '?' );
        else
            urlString.append( '&' );

        try {
            urlString.append( URLEncoder.encode( key, "UTF-8" ) );
            if (value != null) {
                urlString.append( '=' );
                urlString.append( URLEncoder.encode( value.toString(), "UTF-8" ) );
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException( "UTF-8 unsupported by VM", e );
        }

        return urlString.toString();
    }

    /**
     * Concatenate the given paths by gluing them together and making sure there is only one slash separating them.
     *
     * @param paths The path to glue together.
     *
     * @return The glued together path.
     */
    public static String concat(String... paths) {

        if (paths.length == 0)
            return "";

        String base = paths[0] == null? "": paths[0];
        if (paths.length == 1)
            return base;

        // Glue the other paths.
        String[] otherPaths = new String[paths.length - 1];
        System.arraycopy( paths, 1, otherPaths, 0, paths.length - 1 );
        String otherPathsGlued = concat( otherPaths );

        // Glue the base onto the other paths.
        StringBuilder glued = new StringBuilder( base );
        glued.append( (base.charAt( base.length() - 1 ) == '/' || otherPathsGlued.length() == 0? "": '/') );
        if (otherPathsGlued.length() > 0)
            glued.append( ((otherPathsGlued.charAt( 0 ) == '/')? otherPathsGlued.substring( 1 ): otherPathsGlued) );

        return glued.toString();
    }
}
