/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.j2ee;

import java.security.Principal;
import javax.management.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.security.SimplePrincipal;


public class SecurityManagerUtils {

    private static final Log LOG = LogFactory.getLog( SecurityManagerUtils.class );

    private SecurityManagerUtils() {

        // empty
    }

    /**
     * Flushes the JBoss security manager credential cache for a certain principal. This method is accessing the JBoss security manager via
     * JMX.
     */
    public static void flushCredentialCache(String login, String securityDomain) {

        LOG.debug( "flush credential cache for " + login + " on security domain " + securityDomain );
        Principal user = new SimplePrincipal( login );
        ObjectName jaasMgr;
        try {
            jaasMgr = new ObjectName( "jboss.security:service=JaasSecurityManager" );
        } catch (MalformedObjectNameException e) {
            String msg = "ObjectName error: " + e.getMessage();
            LOG.error( msg );
            throw new RuntimeException( msg, e );
        } catch (NullPointerException e) {
            throw new RuntimeException( "NPE: " + e.getMessage(), e );
        }
        Object[] params = { securityDomain, user };
        String[] signature = { String.class.getName(), Principal.class.getName() };
        MBeanServer server = MBeanServerLocator.locateJBoss();
        try {
            server.invoke( jaasMgr, "flushAuthenticationCache", params, signature );
        } catch (InstanceNotFoundException e) {
            String msg = "instance not found: " + e.getMessage();
            LOG.error( msg );
            throw new RuntimeException( msg, e );
        } catch (MBeanException e) {
            String msg = "mbean error: " + e.getMessage();
            LOG.error( msg );
            throw new RuntimeException( msg, e );
        } catch (ReflectionException e) {
            String msg = "reflection error: " + e.getMessage();
            LOG.error( msg );
            throw new RuntimeException( msg, e );
        }
    }
}
