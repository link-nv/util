/*
 * Lisu project.
 *
 * Copyright 2010 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.saml;

import static com.google.common.base.Preconditions.*;

import be.fedict.trust.MemoryCertificateRepository;
import be.fedict.trust.TrustValidator;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.lyndir.lhunath.lib.system.logging.Logger;
import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import net.link.util.common.CertificateChain;
import net.link.util.common.DomUtils;
import net.link.util.error.ValidationFailedException;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.binding.security.SAML2HTTPRedirectDeflateSignatureRule;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.security.SecurityPolicy;
import org.opensaml.ws.security.SecurityPolicyResolver;
import org.opensaml.ws.security.provider.BasicSecurityPolicy;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.*;
import org.opensaml.xml.io.*;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.impl.XSAnyBuilder;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.StaticCredentialResolver;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.opensaml.xml.signature.*;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.Element;


/**
 * SAML v2.0 utility class
 */
public abstract class Saml2Utils {

    static final         Logger              logger                   = Logger.get( Saml2Utils.class );
    private static final MarshallerFactory   marshallerFactory        = Configuration.getMarshallerFactory();
    private static final UnmarshallerFactory unmarshallerFactory      = Configuration.getUnmarshallerFactory();
    private static final QName               XML_SCHEMA_INSTANCE_TYPE = new QName( "http://www.w3.org/2001/XMLSchema-instance", "type",
            "xsi" );
    private static final QName               XML_SCHEMA_INSTANCE_NIL  = new QName( "http://www.w3.org/2001/XMLSchema-instance", "nil",
            "xsi" );

    static {
        /*
         * Next is because Sun loves to endorse crippled versions of Xerces.
         */
        System.setProperty( "javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
                "org.apache.xerces.jaxp.validation.XMLSchemaFactory" );
        try {
            DefaultBootstrap.bootstrap();
        }
        catch (ConfigurationException e) {
            throw new RuntimeException( "could not bootstrap the OpenSAML2 library", e );
        }
    }

    public static <T extends XMLObject> T buildXMLObject(QName objectQName) {

        @SuppressWarnings("unchecked")
        XMLObjectBuilder<T> builder = Configuration.getBuilderFactory().getBuilder( objectQName );
        if (builder == null)
            throw new RuntimeException( "Unable to retrieve builder for object QName " + objectQName );

        return builder.buildObject( objectQName.getNamespaceURI(), objectQName.getLocalPart(), objectQName.getPrefix() );
    }

    public static Element marshall(XMLObject samlObject) {

        Marshaller marshaller = marshallerFactory.getMarshaller( samlObject );

        try {
            return marshaller.marshall( samlObject );
        }
        catch (MarshallingException e) {
            throw new RuntimeException( "While marshaling: " + samlObject, e );
        }
    }

    public static <X extends XMLObject> X unmarshall(Element xmlElement) {

        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller( xmlElement );

        try {
            //noinspection unchecked
            return (X) unmarshaller.unmarshall( xmlElement );
        }
        catch (UnmarshallingException e) {
            throw new RuntimeException( "While unmarshaling: " + xmlElement, e );
        }
    }

    public static String deflateAndBase64Encode(SAMLObject message)
            throws IOException {

        logger.dbg( "Deflating and Base64 encoding SAML message" );
        String messageStr = DomUtils.domToString( marshall( message ) );

        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        Deflater deflater = new Deflater( Deflater.DEFLATED, true );
        DeflaterOutputStream deflaterStream = new DeflaterOutputStream( bytesOut, deflater );
        try {
            deflaterStream.write( messageStr.getBytes( Charsets.UTF_8 ) );
            deflaterStream.finish();
        }
        finally {
            deflaterStream.close();
        }

        return Base64.encodeBytes( bytesOut.toByteArray(), Base64.DONT_BREAK_LINES );
    }

    public static Element signAsElement(SignableSAMLObject samlObject, KeyPair signerKeyPair, CertificateChain certificateChain) {

        XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
        SignatureBuilder signatureBuilder = (SignatureBuilder) builderFactory.getBuilder( Signature.DEFAULT_ELEMENT_NAME );
        Signature signature = signatureBuilder.buildObject();
        signature.setCanonicalizationAlgorithm( SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS );

        String algorithm = signerKeyPair.getPrivate().getAlgorithm();
        if ("RSA".equals( algorithm ))
            signature.setSignatureAlgorithm( SignatureConstants.ALGO_ID_SIGNATURE_RSA );
        else if ("DSA".equals( algorithm ))
            signature.setSignatureAlgorithm( SignatureConstants.ALGO_ID_SIGNATURE_DSA );

        BasicX509Credential signingCredential = new BasicX509Credential();
        signingCredential.setPrivateKey( signerKeyPair.getPrivate() );

        if (null != certificateChain) {
            // enable adding the cert.chain as KeyInfo
            ((X509KeyInfoGeneratorFactory) Configuration.getGlobalSecurityConfiguration()
                                                        .getKeyInfoGeneratorManager()
                                                        .getDefaultManager()
                                                        .getFactory( signingCredential )).setEmitEntityCertificateChain( true );
            signingCredential.setEntityCertificateChain( certificateChain.getOrderedCertificateChain() );

            // add certificate chain as keyinfo
            signature.setKeyInfo( getKeyInfo( certificateChain ) );
        } else {
            signingCredential.setPublicKey( signerKeyPair.getPublic() );

            // add public key as keyinfo
            signature.setKeyInfo( getKeyInfo( signerKeyPair.getPublic() ) );
        }

        signature.setSigningCredential( signingCredential );
        samlObject.setSignature( signature );

        // Marshall so it has an XML representation.
        Element samlElement = marshall( samlObject );

        // Sign after marshaling so we can add a signature to the XML representation.
        try {
            Signer.signObject( signature );
        }
        catch (SignatureException e) {
            throw new RuntimeException( e );
        }
        return samlElement;
    }

    private static KeyInfo getKeyInfo(CertificateChain certificateChain) {

        try {
            KeyInfo keyInfo = buildXMLObject( KeyInfo.DEFAULT_ELEMENT_NAME );
            for (X509Certificate certificate : certificateChain)
                KeyInfoHelper.addCertificate( keyInfo, certificate );

            return keyInfo;
        }
        catch (CertificateEncodingException e) {
            throw new RuntimeException( e );
        }
    }

    private static KeyInfo getKeyInfo(PublicKey publicKey) {

        KeyInfo keyInfo = buildXMLObject( KeyInfo.DEFAULT_ELEMENT_NAME );
        KeyInfoHelper.addPublicKey( keyInfo, publicKey );

        return keyInfo;
    }

    /**
     * Signs the given {@link SignableSAMLObject}.
     *
     * @param samlObject       signable SAML object so sign.
     * @param signerKeyPair    keypair used to sign.
     * @param certificateChain optional certificate chain for offline validation
     *
     * @return The signed {@link SignableSAMLObject}, marshaled and serialized.
     */
    public static String sign(SignableSAMLObject samlObject, KeyPair signerKeyPair, CertificateChain certificateChain) {

        Element samlElement = signAsElement( samlObject, signerKeyPair, certificateChain );

        // Dump our XML element to a string.
        return DomUtils.domToString( samlElement );
    }

    /**
     * Validate the signature on specified {@link Signature}.
     *
     *
     * @param signature           Signature to validate if not using HTTP Redirect binding.
     * @param request             Request to obtain signature from if using HTTP Redirect binding.
     * @param trustedCertificates Certificates that are trusted.  The signature must validate against one of these.
     *
     * @throws ValidationFailedException The signature could not be trusted.
     */
    public static CertificateChain validateSignature(Signature signature, HttpServletRequest request,
                                                     Collection<X509Certificate> trustedCertificates)
            throws ValidationFailedException {

        if (signature != null)
            return validateSignature( signature, trustedCertificates );

        // No signature inside the SAML object, must be in the queryString (SAML HTTP Redirect Binding)
        validateSignature(
                checkNotNull( request, "No signature found in SAML object and no query data provided to search for a signature." ),
                trustedCertificates );

        return null;
    }

    private static CertificateChain validateSignature(Signature signature, Collection<X509Certificate> trustedCertificates)
            throws ValidationFailedException {

        logger.dbg( "validate[HTTP POST], Signature:\n%s", DomUtils.domToString( signature.getDOM(), true ) );

        try {
            CertificateChain certificateChain = new CertificateChain( KeyInfoHelper.getCertificates( signature.getKeyInfo() ) );
            List<PublicKey> publicKeys = KeyInfoHelper.getPublicKeys( signature.getKeyInfo() );
            BasicX509Credential credential = new BasicX509Credential();
            if (!certificateChain.isEmpty())
                credential.setPublicKey( certificateChain.getIdentityCertificate().getPublicKey() );
            else if (!publicKeys.isEmpty() && publicKeys.size() == 1)
                credential.setPublicKey( publicKeys.get( 0 ) );
            else
                throw new ValidationException( "Failed to validate XML Signature, no suitable KeyInfo found..." );

            // Validate the profile.
            SAMLSignatureProfileValidator pv = new SAMLSignatureProfileValidator();
            pv.validate( signature );

            // Validate the signature.
            SignatureValidator sigValidator = new SignatureValidator( credential );
            sigValidator.validate( signature );

            // Validate the certificate chain.
            if (trustedCertificates != null && !trustedCertificates.isEmpty())
                // Allow no trusted certificates: Beware, the chain has not been validated.
                // The application should validate it manually, eg. using XKMS.
                validateCertificateChain( certificateChain, trustedCertificates );

            return certificateChain;
        }
        catch (CertificateException e) {
            throw new ValidationFailedException( e );
        }
        catch (KeyException e) {
            throw new ValidationFailedException( e );
        }
        catch (ValidationException e) {
            throw new ValidationFailedException( e );
        }
    }

    private static void validateSignature(HttpServletRequest request, Collection<X509Certificate> trustedCertificates)
            throws ValidationFailedException {

        logger.dbg( "validate[HTTP Redirect], Query:\n%s", request.getQueryString() );
        if (trustedCertificates == null || trustedCertificates.isEmpty())
            throw new ValidationFailedException( "There are no credentials to validate against." );

        Collection<Credential> trustedCredentials = Collections2.transform( trustedCertificates,
                new Function<X509Certificate, Credential>() {
                    @Override
                    public Credential apply(final X509Certificate from) {

                        return SecurityHelper.getSimpleCredential( from, null );
                    }
                } );
        StaticCredentialResolver credResolver = new StaticCredentialResolver( ImmutableList.copyOf( trustedCredentials ) );
        final SignatureTrustEngine engine = new ExplicitKeySignatureTrustEngine( credResolver,
                Configuration.getGlobalSecurityConfiguration().getDefaultKeyInfoCredentialResolver() );

        BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext = new BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject>();
        messageContext.setInboundMessageTransport( new HttpServletRequestAdapter( request ) );
        messageContext.setPeerEntityRole( SPSSODescriptor.DEFAULT_ELEMENT_NAME );
        messageContext.setSecurityPolicyResolver( new SecurityPolicyResolver() {

            @Override
            public Iterable<SecurityPolicy> resolve(MessageContext criteria)
                    throws org.opensaml.xml.security.SecurityException {

                return Collections.singleton( resolveSingle( criteria ) );
            }

            @Override
            public SecurityPolicy resolveSingle(MessageContext criteria)
                    throws SecurityException {

                SecurityPolicy securityPolicy = new BasicSecurityPolicy();
                securityPolicy.getPolicyRules().add( new SAML2HTTPRedirectDeflateSignatureRule( engine ) );

                return securityPolicy;
            }
        } );

        try {
            new HTTPRedirectDeflateDecoder().decode( messageContext );
        }
        catch (MessageDecodingException e) {
            throw new ValidationFailedException( "Signature validation failed.", e );
        }
        catch (org.opensaml.xml.security.SecurityException e) {
            throw new ValidationFailedException( "Signature validation failed.", e );
        }
    }

    private static void validateCertificateChain(CertificateChain certificateChain, Collection<X509Certificate> trustedCertificates)
            throws ValidationFailedException {

        try {
            MemoryCertificateRepository certificateRepository = new MemoryCertificateRepository();
            for (X509Certificate trustedCertificate : trustedCertificates)
                certificateRepository.addTrustPoint( trustedCertificate );

            TrustValidator trustValidator = new TrustValidator( certificateRepository );
            trustValidator.isTrusted( certificateChain.getOrderedCertificateChain() );
        }
        catch (CertPathValidatorException e) {
            throw new ValidationFailedException(
                    "Certificate chain did not validate against trusted certificates.\nChain:\n" + certificateChain
                    + "\nTrusted Certificates:\n" + trustedCertificates, e );
        }
    }

    /**
     * Validate the specified {@link Assertion}.
     *
     * @param assertion        the assertion to validate
     * @param now              time to validate the conditions in the assertion against.
     * @param expectedAudience expected audience matching the optional audience restriction
     *
     * @throws ValidationFailedException validation failed.
     */
    public static void validateAssertion(Assertion assertion, DateTime now, String expectedAudience)
            throws ValidationFailedException {

        Conditions conditions = assertion.getConditions();
        DateTime notBefore = conditions.getNotBefore();
        DateTime notOnOrAfter = conditions.getNotOnOrAfter();

        logger.dbg( "now: %s", now.toString() );
        logger.dbg( "notBefore: %s", notBefore.toString() );
        logger.dbg( "notOnOrAfter : %s", notOnOrAfter.toString() );

        if (now.isBefore( notBefore )) {
            // time skew
            if (now.plusMinutes( 5 ).isBefore( notBefore ) || now.minusMinutes( 5 ).isAfter( notOnOrAfter ))
                throw new ValidationFailedException(
                        "SAML2 assertion validation audience=" + expectedAudience + " : invalid SAML message timeframe" );
        } else if (now.isBefore( notBefore ) || now.isAfter( notOnOrAfter ))
            throw new ValidationFailedException(
                    "SAML2 assertion validation audience=" + expectedAudience + " : invalid SAML message timeframe" );

        Subject subject = assertion.getSubject();
        if (null == subject)
            throw new ValidationFailedException(
                    "SAML2 assertion validation audience=" + expectedAudience + " : missing Assertion Subject" );

        if (subject.getSubjectConfirmations().isEmpty())
            throw new ValidationFailedException(
                    "SAML2 assertion validation audience=" + expectedAudience + " : missing SubjectConfirmation" );

        SubjectConfirmation subjectConfirmation = subject.getSubjectConfirmations().get( 0 );
        SubjectConfirmationData subjectConfirmationData = subjectConfirmation.getSubjectConfirmationData();
        if (!subjectConfirmationData.getUnknownXMLObjects( KeyInfo.DEFAULT_ELEMENT_NAME ).isEmpty()) {
            // meaning a PublicKey is attached
            if (subjectConfirmationData.getUnknownXMLObjects( KeyInfo.DEFAULT_ELEMENT_NAME ).size() != 1)
                throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience
                                                     + " : more then 1 KeyInfo element in SubjectConfirmationData" );
        }

        if (assertion.getAuthnStatements().isEmpty())
            throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : missing AuthnStatement" );

        AuthnStatement authnStatement = assertion.getAuthnStatements().get( 0 );
        if (null == authnStatement.getAuthnContext())
            throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : missing AuthnContext" );

        if (null == authnStatement.getAuthnContext().getAuthnContextClassRef())
            throw new ValidationFailedException(
                    "SAML2 assertion validation audience=" + expectedAudience + " : missing AuthnContextClassRef" );

        if (expectedAudience != null)
            // Check whether the audience of the response corresponds to the original audience restriction
            validateAudienceRestriction( conditions, expectedAudience );
    }

    /**
     * Validates the audience restriction in specified {@link Conditions} against the specified audience..
     *
     * @param conditions       conditions to validate
     * @param expectedAudience audience expected to be found.
     *
     * @throws ValidationFailedException validation failed.
     */
    public static void validateAudienceRestriction(Conditions conditions, String expectedAudience)
            throws ValidationFailedException {

        List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
        if (audienceRestrictions.isEmpty())
            throw new ValidationFailedException(
                    "SAML2 assertion validation audience=" + expectedAudience + " : no Audience Restrictions found in response assertion" );

        AudienceRestriction audienceRestriction = audienceRestrictions.get( 0 );
        List<Audience> audiences = audienceRestriction.getAudiences();
        if (audiences.isEmpty())
            throw new ValidationFailedException(
                    "SAML2 assertion validation audience=" + expectedAudience + " : no Audiences found in AudienceRestriction" );

        Audience audience = audiences.get( 0 );
        if (!expectedAudience.equals( audience.getAudienceURI() ))
            throw new ValidationFailedException(
                    "SAML2 assertion validation: audience name not correct, expected: " + expectedAudience + " was: "
                    + audience.getAudienceURI() );
    }

    /**
     * Generates an XML {@link XSAny} object filled with specified value.
     *
     * @param attributeValue attribute value to convert
     *
     * @return converted {@link XMLObject}
     */
    public static XMLObject toXmlObject(Object attributeValue) {

        logger.dbg( "converting value %s to XML", attributeValue );

        XSAnyBuilder anyBuilder = (XSAnyBuilder) Configuration.getBuilderFactory().getBuilder( XSAny.TYPE_NAME );
        XSAny anyValue = anyBuilder.buildObject( AttributeValue.DEFAULT_ELEMENT_NAME, XSAny.TYPE_NAME );

        if (attributeValue == null) {
            anyValue.getUnknownAttributes().put( XML_SCHEMA_INSTANCE_NIL, "true" );
            return anyValue;
        }

        String xsType, xsValue = String.valueOf( attributeValue );
        if (Boolean.class.isAssignableFrom( attributeValue.getClass() ))
            xsType = "xs:boolean";
        else if (Integer.class.isAssignableFrom( attributeValue.getClass() ))
            xsType = "xs:integer";
        else if (Long.class.isAssignableFrom( attributeValue.getClass() ))
            xsType = "xs:long";
        else if (Short.class.isAssignableFrom( attributeValue.getClass() ))
            xsType = "xs:short";
        else if (Byte.class.isAssignableFrom( attributeValue.getClass() ))
            xsType = "xs:byte";
        else if (Float.class.isAssignableFrom( attributeValue.getClass() ))
            xsType = "xs:float";
        else if (Double.class.isAssignableFrom( attributeValue.getClass() ))
            xsType = "xs:double";
        else if (Date.class.isAssignableFrom( attributeValue.getClass() )) {
            xsType = "xs:dateTime";
            xsValue = new DateTime( ((Date) attributeValue).getTime() ).toString();
        } else
            xsType = "xs:string";

        anyValue.getUnknownAttributes().put( XML_SCHEMA_INSTANCE_TYPE, xsType );
        anyValue.setTextContent( xsValue );
        logger.dbg( "converting value %s of type %s to XML: xsType = %s, xsValue = %s", attributeValue, attributeValue.getClass(), xsType,
                xsValue );

        return anyValue;
    }

    /**
     * Returns the value inside specified {@link XMLObject}.
     *
     * @param attributeValue {@link XMLObject} to be converted.
     *
     * @return converted java object.
     */
    public static Serializable toJavaObject(XMLObject attributeValue) {

        if (Boolean.valueOf( attributeValue.getDOM().getAttributeNS( "http://www.w3.org/2001/XMLSchema-instance", "nil" ) ))
            return null;

        String xsType = attributeValue.getDOM().getAttributeNS( "http://www.w3.org/2001/XMLSchema-instance", "type" );
        String xsValue = attributeValue.getDOM().getTextContent();

        if ("xs:boolean".equals( xsType ))
            return Boolean.valueOf( xsValue );
        else if ("xs:integer".equals( xsType ))
            return Integer.valueOf( xsValue );
        else if ("xs:long".equals( xsType ))
            return Long.valueOf( xsValue );
        else if ("xs:short".equals( xsType ))
            return Short.valueOf( xsValue );
        else if ("xs:byte".equals( xsType ))
            return Byte.valueOf( xsValue );
        else if ("xs:float".equals( xsType ))
            return Float.valueOf( xsValue );
        else if ("xs:double".equals( xsType ))
            return Double.valueOf( xsValue );
        else if ("xs:dateTime".equals( xsType ))
            return new DateTime( xsValue ).toDate();
        else if ("xs:string".equals( xsType ))
            return xsValue;

        throw new IllegalArgumentException(
                "XML Type (xsi:type=" + xsType + ") of attribute value (text=" + xsValue + ") not understood." );
    }
}
