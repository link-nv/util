/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.filter;

import java.util.Map.Entry;


public interface MapEntryFilter<KeyType, ValueType> {

    boolean isAllowed(Entry<KeyType, ValueType> element);
}
