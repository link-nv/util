/*
 * Lisu project.
 *
 * Copyright 2010 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.saml;

public class ValidationFailedException extends Exception {

    public ValidationFailedException(String message) {

        super( message );
    }

    public ValidationFailedException(String message, Throwable cause) {

        super( message, cause );
    }

    public ValidationFailedException(Throwable cause) {

        super( cause );
    }
}
