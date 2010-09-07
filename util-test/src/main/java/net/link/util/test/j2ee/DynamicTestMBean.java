/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.test.j2ee;

import java.util.HashMap;
import java.util.Map;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.MBeanInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Dummy MBean for unit testing purposes.
 *
 * @author fcorneli
 */
public class DynamicTestMBean implements DynamicMBean {

    private static final Log LOG = LogFactory.getLog( DynamicTestMBean.class );

    private Map<String, MBeanActionHandler> actionHandlers = new HashMap<String, MBeanActionHandler>();

    /**
     * Registers an action handler that this dynamic test MBean will be using when invoking actions.
     */
    public void registerActionHandler(String actionName, MBeanActionHandler action) {

        if (actionHandlers.containsKey( actionName ))
            throw new IllegalStateException( "already registered mbean action: " + actionName );
        actionHandlers.put( actionName, action );
    }

    @SuppressWarnings("unused")
    public Object getAttribute(String attribute) {

        return null;
    }

    @SuppressWarnings("unused")
    public AttributeList getAttributes(String[] attributes) {

        return null;
    }

    public MBeanInfo getMBeanInfo() {

        return new MBeanInfo( this.getClass().getName(), "test", null, null, null, null );
    }

    public Object invoke(String actionName, Object[] params, @SuppressWarnings("unused") String[] signature) {

        LOG.debug( "invoked: " + actionName );
        MBeanActionHandler actionHandler = actionHandlers.get( actionName );
        if (null == actionHandler)
            return null;
        Object result = actionHandler.invoke( params );
        return result;
    }

    @SuppressWarnings("unused")
    public void setAttribute(Attribute attribute) {

    }

    @SuppressWarnings("unused")
    public AttributeList setAttributes(AttributeList attributes) {

        return null;
    }
}
