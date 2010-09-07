/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.servlet.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * Specifies that a component should be outjected from the annotated field.
 *
 * @author Gavin King
 */
@Target({ FIELD })
@Retention(RUNTIME)
@Documented
public @interface Out {

    /**
     * The context variable name. Defaults to the name of the annotated field.
     */
    String value() default "";

    /**
     * Specifies that the outjected value must not be null, by default.
     */
    boolean required() default true;
}
