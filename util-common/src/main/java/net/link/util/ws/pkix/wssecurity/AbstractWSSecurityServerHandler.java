/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.ws.pkix.wssecurity;

import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import net.link.util.common.DomUtils;
import net.link.util.ws.pkix.ClientCrypto;
import net.link.util.ws.pkix.ServerCrypto;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.*;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.ws.security.message.token.Timestamp;
import org.apache.ws.security.util.WSSecurityUtil;
import org.joda.time.DateTime;
import org.joda.time.Instant;


/**
 * JAX-WS SOAP Handler that provides WS-Security server-side verification.
 *
 * @author fcorneli
 */
public abstract class AbstractWSSecurityServerHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Log LOG = LogFactory.getLog( AbstractWSSecurityServerHandler.class );

    public static final String CERTIFICATE_PROPERTY = AbstractWSSecurityServerHandler.class + ".x509";

    public static final String SIGNED_ELEMENTS_CONTEXT_KEY = AbstractWSSecurityServerHandler.class + ".signed.elements";

    @PostConstruct
    public void postConstructCallback() {

        System.setProperty( "com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "true" );
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

        SOAPPart soapPart = soapMessageContext.getMessage().getSOAPPart();
        if ((Boolean) soapMessageContext.get( MessageContext.MESSAGE_OUTBOUND_PROPERTY ))
            handleOutboundDocument( soapPart, soapMessageContext );
        else
            handleInboundDocument( soapPart, soapMessageContext );

        return true;
    }

    /**
     * Handles the outbound SOAP message. Adds the WS Security Header containing a signed timestamp, and signed SOAP body.
     */
    private void handleOutboundDocument(SOAPPart document, @SuppressWarnings("unused") SOAPMessageContext soapMessageContext) {

        LOG.debug( "handle outbound document" );

        LOG.debug( "adding WS-Security SOAP header" );

        WSSecHeader wsSecHeader = new WSSecHeader();
        wsSecHeader.insertSecurityHeader( document );
        WSSecSignature wsSecSignature = new WSSecSignature();
        wsSecSignature.setKeyIdentifierType( WSConstants.BST_DIRECT_REFERENCE );
        WSSecurityConfigurationService wsSecurityConfigurationService = getConfiguration();
        Crypto crypto = new ClientCrypto( wsSecurityConfigurationService.getCertificate(), wsSecurityConfigurationService.getPrivateKey() );
        try {
            wsSecSignature.prepare( document, crypto, wsSecHeader );

            SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants( document.getDocumentElement() );

            Vector<WSEncryptionPart> wsEncryptionParts = new Vector<WSEncryptionPart>();
            WSEncryptionPart wsEncryptionPart = new WSEncryptionPart( soapConstants.getBodyQName().getLocalPart(),
                                                                      soapConstants.getEnvelopeURI(), "Content" );
            wsEncryptionParts.add( wsEncryptionPart );

            WSSecTimestamp wsSecTimeStamp = new WSSecTimestamp();
            wsSecTimeStamp.setTimeToLive( 0 );
            /*
             * If ttl is zero then there will be no Expires element within the Timestamp. Eventually we want to let the service itself
             * decide how long the message validity period is.
             */
            wsSecTimeStamp.prepare( document );
            wsSecTimeStamp.prependToHeader( wsSecHeader );
            wsEncryptionParts.add( new WSEncryptionPart( wsSecTimeStamp.getId() ) );

            wsSecSignature.addReferencesToSign( wsEncryptionParts, wsSecHeader );

            wsSecSignature.prependToHeader( wsSecHeader );

            wsSecSignature.prependBSTElementToHeader( wsSecHeader );

            wsSecSignature.computeSignature();
        }
        catch (WSSecurityException e) {
            throw new RuntimeException( e );
        }

        LOG.debug( "document: " + DomUtils.domToString( document ) );
    }

    @SuppressWarnings("unchecked")
    private void handleInboundDocument(SOAPPart document, SOAPMessageContext soapMessageContext) {

        LOG.debug( "WS-Security header validation" );
        WSSecurityEngine securityEngine = WSSecurityEngine.getInstance();
        WSSecurityConfigurationService wsSecurityConfigurationService = getConfiguration();
        Crypto crypto = new ServerCrypto();

        Vector<WSSecurityEngineResult> wsSecurityEngineResults;
        try {
            wsSecurityEngineResults = securityEngine.processSecurityHeader( document, null, null, crypto );
        }
        catch (WSSecurityException e) {
            throw SOAPUtils.createSOAPFaultException( "The signature or decryption was invalid", "FailedCheck", e );
        }
        LOG.debug( "results: " + wsSecurityEngineResults );
        if (null == wsSecurityEngineResults) {
            if (isInboundSignatureOptional()) {
                LOG.debug( "inbound message is set to optional signed" );
                return;
            }
            throw SOAPUtils.createSOAPFaultException( "An error was discovered processing the <wsse:Security> header.", "InvalidSecurity" );
        }
        Timestamp timestamp = null;
        Set<String> signedElements = null;
        for (WSSecurityEngineResult result : wsSecurityEngineResults) {
            Set<String> resultSignedElements = (Set<String>) result.get( WSSecurityEngineResult.TAG_SIGNED_ELEMENT_IDS );
            if (null != resultSignedElements)
                signedElements = resultSignedElements;
            X509Certificate certificate = (X509Certificate) result.get( WSSecurityEngineResult.TAG_X509_CERTIFICATE );
            if (null != certificate)
                setCertificate( soapMessageContext, certificate );

            Timestamp resultTimestamp = (Timestamp) result.get( WSSecurityEngineResult.TAG_TIMESTAMP );
            if (null != resultTimestamp)
                timestamp = resultTimestamp;
        }

        if (null == signedElements)
            throw SOAPUtils.createSOAPFaultException( "The signature or decryption was invalid", "FailedCheck" );
        LOG.debug( "signed elements: " + signedElements );
        soapMessageContext.put( SIGNED_ELEMENTS_CONTEXT_KEY, signedElements );

        /*
         * Validate certificate.
         */
        X509Certificate certificate = getCertificate( soapMessageContext );
        if (null == certificate)
            throw SOAPUtils.createSOAPFaultException( "missing X509Certificate in WS-Security header", "InvalidSecurity" );
        if (!wsSecurityConfigurationService.validateCertificate( certificate ))
            throw SOAPUtils.createSOAPFaultException( "invalid X509Certificate in WS-Security header", "InvalidSecurity" );

        /*
         * Check timestamp.
         */
        if (null == timestamp)
            throw SOAPUtils.createSOAPFaultException( "missing Timestamp in WS-Security header", "InvalidSecurity" );
        String timestampId = timestamp.getID();
        if (!signedElements.contains( timestampId ))
            throw SOAPUtils.createSOAPFaultException( "Timestamp not signed", "FailedCheck" );
        Calendar created = timestamp.getCreated();
        long maxOffset = wsSecurityConfigurationService.getMaximumWsSecurityTimestampOffset();
        DateTime createdDateTime = new DateTime( created );
        Instant createdInstant = createdDateTime.toInstant();
        Instant nowInstant = new DateTime().toInstant();
        long offset = Math.abs( createdInstant.getMillis() - nowInstant.getMillis() );
        if (offset > maxOffset) {
            LOG.debug( "timestamp offset: " + offset );
            LOG.debug( "maximum allowed offset: " + maxOffset );
            throw SOAPUtils.createSOAPFaultException( "WS-Security Created Timestamp offset exceeded", "FailedCheck" );
        }
    }

    private static void setCertificate(SOAPMessageContext context, X509Certificate certificate) {

        context.put( CERTIFICATE_PROPERTY, certificate );
        context.setScope( CERTIFICATE_PROPERTY, Scope.APPLICATION );
    }

    /**
     * Gives back the X509 certificate that was set previously by a WS-Security handler.
     */
    public static X509Certificate getCertificate(SOAPMessageContext context) {

        return (X509Certificate) context.get( CERTIFICATE_PROPERTY );
    }

    /**
     * Gives back the X509 certificate that was set previously by a WS-Security handler.
     */
    public static X509Certificate getCertificate(WebServiceContext context) {

        return (X509Certificate) context.getMessageContext().get( CERTIFICATE_PROPERTY );
    }

    /**
     * Checks whether a WS-Security handler did verify that the element with given Id was signed correctly.
     */
    public static boolean isSignedElement(String id, SOAPMessageContext context) {

        @SuppressWarnings("unchecked")
        Set<String> signedElements = (Set<String>) context.get( SIGNED_ELEMENTS_CONTEXT_KEY );
        if (null == signedElements)
            return false;

        return signedElements.contains( id );
    }

    protected abstract boolean isInboundSignatureOptional();

    protected abstract WSSecurityConfigurationService getConfiguration();
}
