/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.servlet;

/**
 * Wrapper class that holds a name-value pair for error messages.
 *
 * @author wvdhaute
 */
public class ErrorMessage {

    private String name;
    private String message;
    private Exception error;

    public ErrorMessage(String message) {

        name = "ErrorMessage";
        this.message = message;
    }

    public ErrorMessage(String name, String message) {

        this.name = name;
        this.message = message;
    }

    public ErrorMessage(Exception error) {

        this( error.getLocalizedMessage() );
        this.error = error;
    }

    public String getName() {

        return name;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public Exception getError() {

        return error;
    }
}
