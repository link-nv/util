/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.servlet;

import com.google.common.base.Throwables;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.http.*;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link ServletUtils}</h2>
 *
 * <p>
 * [description / usage].
 * </p>
 *
 * <p>
 * <i>Mar 27, 2009</i>
 * </p>
 *
 * @author lhunath
 */
public class ServletUtils {

    static void writeBasicErrorPage(HttpServletResponse response, ErrorMessage... errorMessages)
            throws IOException {

        response.setContentType( "text/html" );
        response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
        PrintWriter out = response.getWriter();
        out.println( "<html>" );
        {
            out.println( "<head><title>Error</title></head>" );
            out.println( "<body>" );
            {
                out.println( "<h1>Error(s)</h1>" );
                out.println( "<p>" );
                {
                    for (ErrorMessage errorMessage : errorMessages)
                        out.println( errorMessage.getMessage() + "</br>" );
                }
                out.println( "</p>" );
            }
            out.println( "</body>" );
        }
        out.println( "</html>" );
        out.close();
    }

    /**
     * Redirects to the specified error page. The errorMessages entries contain as key the name of the error message attribute that will be
     * pushed on the session. The attribute value will be looked up if a resource bundle is specified, else directly pushed onto the
     * session.
     *
     * @return A string fit for writing to your logger that describes the error given in the parameters.
     *
     * @throws IOException
     */
    public static String redirectToErrorPage(HttpServletRequest request, HttpServletResponse response, String errorPage,
                                             @Nullable String resourceBundleName, ErrorMessage... errorMessages)
            throws IOException {

        HttpSession session = request.getSession();
        ResourceBundle resourceBundle = null;
        if (null != resourceBundleName) {
            Locale locale = request.getLocale();
            try {
                resourceBundle = ResourceBundle.getBundle( resourceBundleName, locale, Thread.currentThread().getContextClassLoader() );
            } catch (MissingResourceException e) {
                resourceBundle = null;
            }
        }
        StringBuilder logMessage = new StringBuilder();
        for (ErrorMessage errorMessage : errorMessages) {
            if (null != resourceBundle)
                try {
                    errorMessage.setMessage( resourceBundle.getString( errorMessage.getMessage() ) );
                } catch (MissingResourceException e) {
                }

            if (logMessage.length() > 0)
                logMessage.append( '\n' );
            logMessage.append( errorMessage.getName() ).append( ": " ).append( errorMessage.getMessage() );
            if (errorMessage.getError() != null)
                logMessage.append( '\n' ).append( Throwables.getStackTraceAsString( errorMessage.getError() ) );
        }
        if (null == errorPage)
            /*
             * If no error page specified, spit out a basic HTML page containing the error message.
             */
            writeBasicErrorPage( response, errorMessages );
        else {
            for (ErrorMessage errorMessage : errorMessages)
                session.setAttribute( errorMessage.getName(), errorMessage.getMessage() );
            response.sendRedirect( errorPage );
        }

        return logMessage.toString();
    }
}
