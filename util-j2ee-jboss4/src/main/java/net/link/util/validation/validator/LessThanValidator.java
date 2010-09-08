/*
 * SafeOnline project.
 *
 * Copyright 2005-2007 Frank Cornelis H.S.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.validation.validator;

import net.link.util.validation.annotation.LessThan;


public class LessThanValidator implements Validator<LessThan> {

    public void validate(Object value, int parameterIdx, LessThan parameterAnnotation, ValidatorResult result) {

        String name = parameterAnnotation.name();
        if ("".equals( name ))
            name = "parameter " + (parameterIdx + 1);
        if (!(value instanceof Integer))
            throw new IllegalStateException( "parameter is not an integer" );
        double numValue = ((Integer) value).doubleValue();
        double lessThanValue = parameterAnnotation.value();
        if (numValue >= lessThanValue)
            result.addResult( "the given parameter \"" + name + "\" is not less than " + numValue );
    }
}
