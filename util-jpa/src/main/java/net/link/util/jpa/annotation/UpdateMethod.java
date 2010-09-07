/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.jpa.annotation;

import java.lang.annotation.*;


/**
 * Used to mark a method as being a JPA update query execution method.
 *
 * @author fcorneli
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface UpdateMethod {

    /**
     * The name of the named query to execute.
     */
    String value();
}
