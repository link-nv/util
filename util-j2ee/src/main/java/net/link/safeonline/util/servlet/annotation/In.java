/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.util.servlet.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * Specifies that a component should be injected to the annotated field.
 */
@Target({ FIELD })
@Retention(RUNTIME)
@Documented
public @interface In {

    /**
     * The context variable name. Defaults to the name of the annotated field.
     */
    String value() default "";

    /**
     * Specifies that the injected value must not be null, by default.
     */
    boolean required() default true;
}
