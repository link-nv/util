/*
 * SafeOnline project.
 *
 * Copyright 2006-2010 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.common;

import java.util.*;


/**
 * <h2>{@link PropertiesResourceBundle}</h2>
 * <p/>
 * <p>
 * <i>Mar 11, 2010</i>
 * </p>
 *
 * @author lhunath
 */
public class PropertiesResourceBundle extends ResourceBundle {

    private Properties props;

    PropertiesResourceBundle(Properties props) {

        this.props = props;
    }

    @Override
    protected Object handleGetObject(String key) {

        return props.getProperty( key );
    }

    @Override
    public Enumeration<String> getKeys() {

        return Collections.enumeration( props.stringPropertyNames() );
    }
}
