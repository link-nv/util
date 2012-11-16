/*
 * Copyright (c) 2006-2011 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 ******************************************************************************/

package net.link.util.saml;

import java.util.List;
import net.link.util.exception.ValidationFailedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.ReadableDateTime;
import org.opensaml.saml1.core.*;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSAny;


/**
 * SAML v2.0 utility class
 */
public abstract class Saml1Utils extends SamlUtils {

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
                    throw new ValidationFailedException(
                            "SAML2 assertion validation audience=" + expectedAudience + " : invalid SAML message timeframe" );
            } else if (now.isBefore( notBefore ) || now.isAfter( notOnOrAfter ))
                throw new ValidationFailedException(
                        "SAML2 assertion validation audience=" + expectedAudience + " : invalid SAML message timeframe" );
        }
        if (assertion.getAuthenticationStatements().isEmpty())
            throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : missing AuthnStatement" );
        AuthenticationStatement authnStatement = assertion.getAuthenticationStatements().get( 0 );

        Subject subject = authnStatement.getSubject();
        if (null == subject)
            throw new ValidationFailedException(
                    "SAML2 assertion validation audience=" + expectedAudience + " : missing Assertion Subject" );

        SubjectConfirmation subjectConfirmation = subject.getSubjectConfirmation();

        if (null == authnStatement.getAuthenticationMethod())
            throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : missing AuthenticationMethod" );

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

        List<AudienceRestrictionCondition> audienceRestrictions = conditions.getAudienceRestrictionConditions();
        if (audienceRestrictions.isEmpty())
            throw new ValidationFailedException(
                    "SAML2 assertion validation audience=" + expectedAudience + " : no Audience Restrictions found in response assertion" );

        AudienceRestrictionCondition audienceRestriction = audienceRestrictions.get( 0 );
        List<Audience> audiences = audienceRestriction.getAudiences();
        if (audiences.isEmpty())
            throw new ValidationFailedException(
                    "SAML2 assertion validation audience=" + expectedAudience + " : no Audiences found in AudienceRestriction" );

        Audience audience = audiences.get( 0 );
        if (!expectedAudience.equals( audience.getUri() ))
            throw new ValidationFailedException(
                    "SAML2 assertion validation: audience name not correct, expected: " + expectedAudience + " was: "
                    + audience.getUri() );
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
