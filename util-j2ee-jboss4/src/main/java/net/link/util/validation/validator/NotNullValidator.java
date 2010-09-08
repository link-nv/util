/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.validation.validator;

import net.link.util.validation.annotation.NotNull;


public class NotNullValidator implements Validator<NotNull> {

    public void validate(Object value, int parameterIdx, NotNull parameterAnnotation, ValidatorResult result) {

        String name = parameterAnnotation.value();
        if ("".equals(name))
            name = "parameter " + (parameterIdx + 1);

        if (null == value) {
            result.addResult("the given parameter \"" + name + "\" is null");
            return;
        }
    }
}
