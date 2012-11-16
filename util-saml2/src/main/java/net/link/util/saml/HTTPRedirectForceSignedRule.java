package net.link.util.saml;

import com.lyndir.lhunath.opal.system.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.binding.security.SAML2HTTPRedirectDeflateSignatureRule;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.signature.SignatureTrustEngine;


/**
 * Extends SAML2HTTPRedirectDeflateSignatureRule in making sure there is a signature placed instead of just silently ignoring that and
 * skipping validation.
 */
public class HTTPRedirectForceSignedRule extends SAML2HTTPRedirectDeflateSignatureRule {

    private final Logger log = Logger.get( HTTPRedirectForceSignedRule.class );

    /**
     * Constructor.
     *
     * @param engine the trust engine to use
     */
    public HTTPRedirectForceSignedRule(SignatureTrustEngine engine) {

        super( engine );
    }

    @Override
    public void evaluate(final MessageContext messageContext)
            throws SecurityPolicyException {

        // first see if a signature is in place...
        if (!(messageContext instanceof SAMLMessageContext)) {
            log.err( "Invalid message context type, this policy rule only supports SAMLMessageContext" );
            throw new SecurityPolicyException( "Invalid message context type, this policy rule only supports SAMLMessageContext" );
        }

        SAMLMessageContext samlMsgCtx = (SAMLMessageContext) messageContext;
        if (!isMessageSigned( samlMsgCtx )) {
            throw new SecurityPolicyException( "Inbound and/or outbound message was not signed!" );
        }

        // ok, validate it
        super.evaluate( messageContext );
    }

    /**
     * Determine whether the inbound or outbound message is signed.
     *
     * @param messageContext the message context being evaluated
     *
     * @return true if the inbound or outbound message is signed, otherwise false
     */
    protected boolean isMessageSigned(SAMLMessageContext messageContext)
            throws SecurityPolicyException {

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
        HttpServletRequestAdapter requestAdapter;
        if (null != messageContext.getInboundMessageTransport()) {
            requestAdapter = (HttpServletRequestAdapter) messageContext.getInboundMessageTransport();
        } else if (null != messageContext.getOutboundMessageTransport()) {
            requestAdapter = (HttpServletRequestAdapter) messageContext.getOutboundMessageTransport();
        } else {
            return false;
        }
        HttpServletRequest request = requestAdapter.getWrappedRequest();

        // check "Signature", "SigAlg", "<signed-content>" present
        byte[] signature = getSignature( request );
        if (signature == null || signature.length == 0) {
            throw new SecurityPolicyException( "No \"Signature\" request parameter present!" );
        }

        String signatureAlgorithm = getSignatureAlgorithm( request );
        if (signatureAlgorithm == null || signatureAlgorithm.isEmpty()) {
            throw new SecurityPolicyException( "No \"SigAlg\" request parameter present!" );
        }

        byte[] signedContent = getSignedContent( request );
        if (signedContent == null || signedContent.length == 0) {
            throw new SecurityPolicyException( "No \"signed content\" ?!!" );
        }

        return true;
    }
}
