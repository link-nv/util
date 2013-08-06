/*
 * SafeOnline project.
 *
 * Copyright 2006-2010 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.common;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;


/**
 * <h2>{@link XMLResourceBundle}<br>
 * <sub>A {@link ResourceBundle} that loads XML properties files.</sub></h2>
 * <p/>
 * <p>
 * <i>Mar 11, 2010</i>
 * </p>
 *
 * @author lhunath
 */
public class XMLResourceBundle extends PropertiesResourceBundle {

    XMLResourceBundle(Properties props) {

        super( props );
    }

    /**
     * @see ResourceBundle#getBundle(String)
     */
    public static ResourceBundle getXMLBundle(String baseName) {

        return getBundle( baseName, new Control() );
    }

    /**
     * @see ResourceBundle#getBundle(String, Locale)
     */
    public static ResourceBundle getXMLBundle(String baseName, Locale locale) {

        return getBundle( baseName, locale, new Control() );
    }

    /**
     * @see ResourceBundle#getBundle(String, Locale, ClassLoader)
     */
    public static ResourceBundle getXMLBundle(String baseName, Locale locale, ClassLoader loader) {

        return getBundle( baseName, locale, loader, new Control() );
    }

    static class Control extends ResourceBundle.Control {

        private static String XML = "xml";

        Control() {

        }

        @Override
        public List<String> getFormats(String baseName) {

            return Collections.singletonList( XML );
        }

        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {

            if (baseName == null || locale == null || format == null || loader == null)
                throw new NullPointerException();
            if (!format.equals( XML ))
                return null;

            String bundleName = toBundleName( baseName, locale );
            String resourceName = toResourceName( bundleName, format );

            URL url = loader.getResource( resourceName );
            if (url == null)
                return null;

            URLConnection connection = url.openConnection();
            if (connection == null)
                return null;
            if (reload)
                connection.setUseCaches( false );

            InputStream stream = connection.getInputStream();
            if (stream == null)
                return null;

            Properties xmlProps = new Properties();
            BufferedInputStream input = new BufferedInputStream( stream );
            try {
                xmlProps.loadFromXML( input );
            }

            finally {
                input.close();
            }

            return new XMLResourceBundle( xmlProps );
        }
    }
}
