/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util.session;

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.spi.LoginModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Dummy JAAS login module. Does nothing but saying 'fine by me'.
 *
 * @author fcorneli
 */
public class DummyLoginModule implements LoginModule {

    private static final Log LOG = LogFactory.getLog( DummyLoginModule.class );

    public boolean abort() {

        LOG.debug( "abort" );
        return true;
    }

    public boolean commit() {

        LOG.debug( "commit" );
        return true;
    }

    @SuppressWarnings("unused")
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {

        LOG.debug( "initialize" );
    }

    public boolean login() {

        LOG.debug( "login" );
        return true;
    }

    public boolean logout() {

        LOG.debug( "logout" );
        return true;
    }
}
