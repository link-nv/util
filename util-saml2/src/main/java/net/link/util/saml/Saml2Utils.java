/*
 * Lisu project.
 *
 * Copyright 2010 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.saml;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import net.link.util.common.CertificateChain;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.ReadableDateTime;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.security.SecurityPolicy;
import org.opensaml.ws.security.SecurityPolicyResolver;
import org.opensaml.ws.security.provider.BasicSecurityPolicy;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.StaticCredentialResolver;
import org.opensaml.xml.signature.*;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;


/**
 * SAML v2.0 utility class
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class Saml2Utils extends SamlUtils {

    /**
     * Checks whether the assertion is well formed and validates against some given data.
     *
     * @param assertion        the assertion to validate
     * @param now              Check whether the assertion is valid at this instant.
     * @param expectedAudience expected audience matching the optional audience restriction
     *
     * @throws ValidationFailedException validation failed.
     */
    public static void validateAssertion(@NotNull Assertion assertion, @Nullable ReadableDateTime now, @Nullable String expectedAudience)
            throws ValidationFailedException {

        Conditions conditions = assertion.getConditions();
        DateTime notBefore = conditions.getNotBefore();
        DateTime notOnOrAfter = conditions.getNotOnOrAfter();

        logger.dbg( "now: %s", now );
        logger.dbg( "notBefore: %s", notBefore );
        logger.dbg( "notOnOrAfter : %s", notOnOrAfter );

        if (now != null) {
            if (now.isBefore( notBefore )) {
                // time skew
                DateTime nowDt = now.toDateTime();
                if (nowDt.plusMinutes( 5 ).isBefore( notBefore ) || nowDt.minusMinutes( 5 ).isAfter( notOnOrAfter ))
                    throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : invalid SAML message timeframe" );
            } else if (now.isBefore( notBefore ) || now.isAfter( notOnOrAfter ))
                throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : invalid SAML message timeframe" );
        }

        Subject subject = assertion.getSubject();
        if (null == subject)
            throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : missing Assertion Subject" );

        if (subject.getSubjectConfirmations().isEmpty())
            throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : missing SubjectConfirmation" );

        SubjectConfirmation subjectConfirmation = subject.getSubjectConfirmations().get( 0 );
        SubjectConfirmationData subjectConfirmationData = subjectConfirmation.getSubjectConfirmationData();
        if (!subjectConfirmationData.getUnknownXMLObjects( KeyInfo.DEFAULT_ELEMENT_NAME ).isEmpty()) {
            // meaning a PublicKey is attached
            if (subjectConfirmationData.getUnknownXMLObjects( KeyInfo.DEFAULT_ELEMENT_NAME ).size() != 1)
                throw new ValidationFailedException(
                        "SAML2 assertion validation audience=" + expectedAudience + " : more then 1 KeyInfo element in SubjectConfirmationData" );
        }

        if (assertion.getAuthnStatements().isEmpty())
            throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : missing AuthnStatement" );

        AuthnStatement authnStatement = assertion.getAuthnStatements().get( 0 );
        if (null == authnStatement.getAuthnContext())
            throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : missing AuthnContext" );

        if (null == authnStatement.getAuthnContext().getAuthnContextClassRef())
            throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : missing AuthnContextClassRef" );

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
    public static void validateAudienceRestriction(@NotNull Conditions conditions, @NotNull String expectedAudience)
            throws ValidationFailedException {

        List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
        if (audienceRestrictions.isEmpty())
            throw new ValidationFailedException(
                    "SAML2 assertion validation audience=" + expectedAudience + " : no Audience Restrictions found in response assertion" );

        AudienceRestriction audienceRestriction = audienceRestrictions.get( 0 );
        List<Audience> audiences = audienceRestriction.getAudiences();
        if (audiences.isEmpty())
            throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : no Audiences found in AudienceRestriction" );

        Audience audience = audiences.get( 0 );
        if (!expectedAudience.equals( audience.getAudienceURI() ))
            throw new ValidationFailedException(
                    "SAML2 assertion validation: audience name not correct, expected: " + expectedAudience + " was: " + audience.getAudienceURI() );
    }

    public static void validateRedirectSignature(HttpServletRequest request, Collection<X509Certificate> trustedCertificates)
            throws ValidationFailedException {

        logger.dbg( "validate[HTTP Redirect], Query:\n%s", request.getQueryString() );
        if (trustedCertificates == null || trustedCertificates.isEmpty())
            throw new ValidationFailedException( "There are no credentials to validate against." );

        Collection<Credential> trustedCredentials = Collections2.transform( trustedCertificates, new Function<X509Certificate, Credential>() {
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
                securityPolicy.getPolicyRules().add( new HTTPRedirectForceSignedRule( engine ) );

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

    /**
     * Validate the signature on specified {@link Signature}.
     *
     * @param signature           Signature to validate if not using HTTP Redirect binding.
     * @param request             Request to obtain signature from if using HTTP Redirect binding.
     * @param trustedCertificates Certificates that are trusted.  The signature must validate against one of these.
     *
     * @return The certificate chain provided by the request or <code>null</code> if the binding does not support passing certificate
     * information. The chain can be empty if the binding supports passing certificate information but none are provided.
     *
     * @throws ValidationFailedException The signature could not be trusted.
     */
    @Nullable
    public static CertificateChain validateSignature(Signature signature, HttpServletRequest request, Collection<X509Certificate> trustedCertificates)
            throws ValidationFailedException {

        if (signature != null)
            return validatePostSignature( signature, trustedCertificates );

        // No signature inside the SAML object, must be in the queryString (SAML HTTP Redirect Binding)
        validateRedirectSignature( checkNotNull( request, "No signature found in SAML object and no query data provided to search for a signature." ),
                trustedCertificates );

        return null;
    }

    /**
     * Generates an XML {@link XSAny} object filled with specified value.
     *
     * @param attributeValue attribute value to convert
     *
     * @return converted {@link XMLObject}
     */
    public static XMLObject toAttributeValue(Object attributeValue) {

        return toAttributeValue( AttributeValue.DEFAULT_ELEMENT_NAME, attributeValue );
    }
}
