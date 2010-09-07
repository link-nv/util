/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util.j2ee;

/**
 * MBean action handler interface.
 *
 * @author fcorneli
 */
public interface MBeanActionHandler {

    Object invoke(Object[] arguments);
}
