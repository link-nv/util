/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.ws.pkix.wssecurity;

import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;


/**
 * JAX-WS SOAP Handler to verify the digestion of the SOAP Body element by the WS-Security signature. We have to postpone this verification
 * until after we know the calling application identity since we need to be able to determine if we need to perform the check on an
 * application basis.
 *
 * @author fcorneli
 */
public abstract class AbstractWSSecurityBodyHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Log LOG = LogFactory.getLog( AbstractWSSecurityBodyHandler.class );

    public Set<QName> getHeaders() {

        return null;
    }

    public void close(MessageContext messageContext) {

    }

    public boolean handleFault(SOAPMessageContext soapMessageContext) {

        return true;
    }

    public boolean handleMessage(SOAPMessageContext soapMessageContext) {

        if ((Boolean) soapMessageContext.get( MessageContext.MESSAGE_OUTBOUND_PROPERTY ))
            // Message is outbound.
            return true;

        if (AbstractWSSecurityServerHandler.getCertificate( soapMessageContext ) == null) {
            // No certificate in message.
            if (!isInboundSignatureOptional())
                throw new RuntimeException( "no certificate found on JAX-WS context" );

            return true;
        }

        // Check whether the SOAP Body has been signed.
        try {
            LOG.debug( "performing message integrity check" );
            SOAPMessage soapMessage = soapMessageContext.getMessage();
            SOAPPart soapPart = soapMessage.getSOAPPart();
            SOAPBody soapBody = soapPart.getEnvelope().getBody();
            String bodyId = soapBody.getAttributeNS( WSConstants.WSU_NS, "Id" );

            if (null == bodyId || 0 == bodyId.length())
                throw SOAPUtils.createSOAPFaultException( "SOAP Body should have a wsu:Id attribute", "FailedCheck" );
            if (!AbstractWSSecurityServerHandler.isSignedElement( bodyId, soapMessageContext ))
                throw SOAPUtils.createSOAPFaultException( "SOAP Body was not signed", "FailedCheck" );
        }
        catch (SOAPException e) {
            throw SOAPUtils.createSOAPFaultException( "error retrieving SOAP Body", "FailedCheck", e );
        }

        return true;
    }

    protected abstract boolean isInboundSignatureOptional();
}
