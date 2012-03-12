package net.link.util.saml;

import com.lyndir.lhunath.opal.system.logging.Logger;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.ws.security.SecurityPolicyRule;
import org.opensaml.ws.transport.http.HTTPInTransport;
import org.opensaml.ws.transport.http.HTTPOutTransport;
import org.opensaml.xml.util.DatatypeHelper;


/**
 * Evaluates the message contect and throws a security policy exception case the message was not signed.
 */
public class HTTPRedirectForceSignedRule implements SecurityPolicyRule {

    private final Logger log = Logger.get( HTTPRedirectForceSignedRule.class );

    @Override
    public void evaluate(final MessageContext messageContext)
            throws SecurityPolicyException {

        if (!(messageContext instanceof SAMLMessageContext)) {
            log.err( "Invalid message context type, this policy rule only supports SAMLMessageContext" );
            throw new SecurityPolicyException( "Invalid message context type, this policy rule only supports SAMLMessageContext" );
        }

        SAMLMessageContext samlMsgCtx = (SAMLMessageContext) messageContext;
        if (!isMessageSigned( samlMsgCtx )) {
            throw new SecurityPolicyException( "Inbound and/or outbound message was not signed!" );
        }
    }

    /**
     * Determine whether the inbound or outbound message is signed.
     *
     * @param messageContext the message context being evaluated
     *
     * @return true if the inbound or outbound message is signed, otherwise false
     */
    protected boolean isMessageSigned(SAMLMessageContext messageContext) {

        SAMLObject samlMessage = null;
        if (null != messageContext.getInboundSAMLMessage()) {

            samlMessage = messageContext.getInboundSAMLMessage();
        } else if (null != messageContext.getOutboundSAMLMessage()) {

            samlMessage = messageContext.getOutboundSAMLMessage();
        }

        if (null != samlMessage) {
            if (samlMessage instanceof SignableSAMLObject) {
                SignableSAMLObject signableMessage = (SignableSAMLObject) samlMessage;
                if (signableMessage.isSigned()) {
                    return true;
                }
            }
        }

        // This handles HTTP-Redirect and HTTP-POST-SimpleSign bindings.
        HTTPInTransport inTransport = (HTTPInTransport) messageContext.getInboundMessageTransport();
        if (null != inTransport) {
            String sigParam = inTransport.getParameterValue( "Signature" );
            return !DatatypeHelper.isEmpty( sigParam );
        }
        HTTPOutTransport outTransport = (HTTPOutTransport) messageContext.getOutboundMessageTransport();
        if (null != outTransport) {
            String sigParam = outTransport.getParameterValue( "Signature" );
            return !DatatypeHelper.isEmpty( sigParam );
        }

        return false;
    }
}
