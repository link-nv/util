/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.valve;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.catalina.Context;
import org.apache.catalina.Realm;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.SimplePrincipal;


/**
 * Tomcat Login Valve. This valve will set the Tomcat servlet container user principal based on a HTTP session context attribute.
 *
 * @author fcorneli
 */
public class LoginValve extends ValveBase {

    private static final Log LOG = LogFactory.getLog( LoginValve.class );

    public LoginValve() {

        LOG.debug( "login valve construction" );
    }

    @Override
    public void invoke(Request request, Response response)
            throws IOException, ServletException {

        LOG.debug( "login valve invoked" );

        HttpServletRequest httpServletRequest = request.getRequest();

        HttpSession httpSession = httpServletRequest.getSession( false );
        if (null != httpSession) {
            LOG.debug( "http session present" );
            String userId = (String) httpSession.getAttribute( "userId" );
            if (null != userId) {
                LOG.debug( "setting user principal to " + userId );
                request.setUserPrincipal( new SimplePrincipal( userId ) );
            }
        }

        LOG.debug( "checking for context presence" );
        Context context = request.getContext();
        if (null != context) {
            LOG.debug( "context name: " + context.getName() );
            LOG.debug( "context info: " + context.getInfo() );
            LOG.debug( "context type: " + context.getClass().getName() );
            Realm realm = context.getRealm();
            if (null != realm) {
                LOG.debug( "realm info: " + realm.getInfo() );
                LOG.debug( "realm type: " + realm.getClass().getName() );
            }
        }

        Valve valve = getNext();
        if (null != valve)
            valve.invoke( request, response );
    }
}
