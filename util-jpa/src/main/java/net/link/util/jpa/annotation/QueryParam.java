/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.jpa.annotation;

import java.lang.annotation.*;


/**
 * Used to mark a method parameter as being using as JPA query injection parameter.
 *
 * @author fcorneli
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface QueryParam {

    /**
     * Name of the parameter within the query.
     */
    String value();
}
