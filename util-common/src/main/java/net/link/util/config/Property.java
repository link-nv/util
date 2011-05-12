/*
 * Copyright (c) 2006-2011 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 ******************************************************************************/

package net.link.util.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * <h2>{@link Property}<br>
 * <sub>[in short] (TODO).</sub></h2>
 *
 * <p>
 * <i>09 15, 2010</i>
 * </p>
 *
 * @author lhunath
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {

    String NONE = "Config.Property.NONE";
    String AUTO = "Config.Property.AUTO";

    boolean required();

    String unset() default NONE;

    EditField editAs() default EditField.TYPE_DEFAULT;

    enum EditField {
        TYPE_DEFAULT, ONE_LINE, MULTI_LINE, BINARY
    }
}
