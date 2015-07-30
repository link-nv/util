/*
 * SafeOnline project.
 *
 * Copyright 2006-2013 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.email;

import javax.mail.PasswordAuthentication;


/**
 * User: gvhoecke <gianni.vanhoecke@lin-k.net>
 * Date: 02/09/13
 * Time: 14:04
 */
public class MailAuthenticator extends javax.mail.Authenticator {

    private final String user;
    private final String password;

    public MailAuthenticator( final String user, final String password ) {

        this.user = user;
        this.password = password;
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {

        return new javax.mail.PasswordAuthentication( user, password );
    }
}
