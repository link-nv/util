/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.test.session;

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.spi.LoginModule;


/**
 * Dummy JAAS login module. Does nothing but saying 'fine by me'.
 *
 * @author fcorneli
 */
public class DummyLoginModule implements LoginModule {

    public boolean abort() {

        return true;
    }

    public boolean commit() {

        return true;
    }

    @SuppressWarnings("unused")
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {

    }

    public boolean login() {

        return true;
    }

    public boolean logout() {

        return true;
    }
}
