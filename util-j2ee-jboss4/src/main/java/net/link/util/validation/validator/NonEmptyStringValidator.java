/*
 * SafeOnline project.
 *
 * Copyright 2005-2007 Frank Cornelis H.S.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.validation.validator;

import net.link.util.validation.annotation.NonEmptyString;


public class NonEmptyStringValidator implements Validator<NonEmptyString> {

    public void validate(Object value, int parameterIdx, NonEmptyString parameterAnnotation, ValidatorResult result) {

        String name = parameterAnnotation.value();
        if ("".equals( name ))
            name = "parameter " + (parameterIdx + 1);

        if (null == value) {
            result.addResult( "the given string parameter \"" + name + "\" is null" );
            return;
        }
        if (!(value instanceof String)) {
            result.addResult( "the given parameter \"" + name + "\" is not a string" );
            return;
        }
        String strValue = (String) value;
        if (0 == strValue.length()) {
            result.addResult( "the given string parameter \"" + name + "\" is empty" );
            return;
        }
    }
}
