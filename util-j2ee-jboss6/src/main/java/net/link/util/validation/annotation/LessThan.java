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
import net.link.util.validation.validator.LessThanValidator;


@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ValidatorAnnotation(LessThanValidator.class)
public @interface LessThan {

    double value();

    String name() default "";
}
