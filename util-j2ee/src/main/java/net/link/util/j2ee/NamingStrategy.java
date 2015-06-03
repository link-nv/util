/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.j2ee;

import java.io.Serializable;
import java.lang.reflect.Field;


/**
 * <h2>{@link NamingStrategy}<br>
 * <sub>Strategy which resolves the JNDI name of the given type.</sub></h2>
 * <p/>
 * <p>
 * <i>Mar 3, 2009</i>
 * </p>
 *
 * @author lhunath
 */
public interface NamingStrategy extends Serializable {

    String calculateName(Class<?> ejbType);

    boolean isSupported(Field field);
}
