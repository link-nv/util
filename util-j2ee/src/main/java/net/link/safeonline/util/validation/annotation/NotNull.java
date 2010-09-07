/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.validation.annotation;

import java.lang.annotation.*;
import net.link.safeonline.util.validation.validator.NotNullValidator;


@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ValidatorAnnotation(NotNullValidator.class)
public @interface NotNull {

    String value() default "";
}
