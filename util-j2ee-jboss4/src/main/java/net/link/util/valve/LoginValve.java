/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.valve;

import com.lyndir.lhunath.opal.system.logging.Logger;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.catalina.*;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.jboss.security.SimplePrincipal;


/**
 * Tomcat Login Valve. This valve will set the Tomcat servlet container user principal based on a HTTP session context attribute.
 *
 * @author fcorneli
 */
public class LoginValve extends ValveBase {

    private static final Logger logger = Logger.get( LoginValve.class );

    public LoginValve() {

        logger.dbg( "login valve construction" );
    }

    @Override
    public void invoke(Request request, Response response)
            throws IOException, ServletException {

        logger.dbg( "login valve invoked" );

        HttpServletRequest httpServletRequest = request.getRequest();

        HttpSession httpSession = httpServletRequest.getSession( false );
        if (null != httpSession) {
            logger.dbg( "http session present" );
            String userId = (String) httpSession.getAttribute( "userId" );
            if (null != userId) {
                logger.dbg( "setting user principal to " + userId );
                request.setUserPrincipal( new SimplePrincipal( userId ) );
            }
        }

        logger.dbg( "checking for context presence" );
        Context context = request.getContext();
        if (null != context) {
            logger.dbg( "context name: " + context.getName() );
            logger.dbg( "context info: " + context.getInfo() );
            logger.dbg( "context type: " + context.getClass().getName() );
            Realm realm = context.getRealm();
            if (null != realm) {
                logger.dbg( "realm info: " + realm.getInfo() );
                logger.dbg( "realm type: " + realm.getClass().getName() );
            }
        }

        Valve valve = getNext();
        if (null != valve)
            valve.invoke( request, response );
    }
}
