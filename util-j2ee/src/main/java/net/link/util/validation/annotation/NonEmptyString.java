/*
 * SafeOnline project.
 *
 * Copyright 2005-2007 Frank Cornelis H.S.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.validation.annotation;

import java.lang.annotation.*;
import net.link.util.validation.validator.NonEmptyStringValidator;


@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ValidatorAnnotation(NonEmptyStringValidator.class)
public @interface NonEmptyString {

    String value() default "";
}
