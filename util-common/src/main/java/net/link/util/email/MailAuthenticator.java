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

    String User;
    String Password;

    public MailAuthenticator(String user, String password) {

        User = user;
        Password = password;
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {

        return new javax.mail.PasswordAuthentication( User, Password );
    }
}
