/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.ws.pkix.wssecurity;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.soap.SOAPFaultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class SOAPUtils {

    static final Logger logger = LoggerFactory.getLogger( SOAPUtils.class );

    private static final SOAPFactory soapFactory;

    static {
        try {
            soapFactory = SOAPFactory.newInstance();
        }
        catch (SOAPException e) {
            throw new RuntimeException( e );
        }
    }

    public static SOAPFaultException createSOAPFaultException(String faultString, String wsseFaultCode) {

        return createSOAPFaultException( faultString, wsseFaultCode, null );
    }

    public static SOAPFaultException createSOAPFaultException(String faultString, String wsseFaultCode, final Throwable cause) {

        logger.warn( "Soap fault: " + faultString, cause );

        try {
            return new SOAPFaultException( soapFactory.createFault( faultString + (cause == null? "": ": " + cause.toString()), new QName(
                    "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", wsseFaultCode, "wsse" ) ) );
        }
        catch (SOAPException e) {
            throw new RuntimeException( e );
        }
    }
}
