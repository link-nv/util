/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util.web.ws;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;


public class TestWSSecurityServerHandler implements SOAPHandler<SOAPMessageContext> {

    @PostConstruct
    public void postConstructCallback() {

    }

    public Set<QName> getHeaders() {

        Set<QName> headers = new HashSet<QName>();
        headers.add( new QName( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security" ) );
        return headers;
    }

    public void close(@SuppressWarnings("unused") MessageContext messageContext) {

        // empty
    }

    public boolean handleFault(@SuppressWarnings("unused") SOAPMessageContext soapMessageContext) {

        return true;
    }

    public boolean handleMessage(SOAPMessageContext soapMessageContext) {

        return true;
    }
}
