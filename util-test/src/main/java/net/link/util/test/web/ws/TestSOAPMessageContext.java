/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.test.web.ws;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class TestSOAPMessageContext implements SOAPMessageContext {

    private static final Log LOG = LogFactory.getLog( TestSOAPMessageContext.class );

    private SOAPMessage message;

    private final Map<String, Object> properties;

    public TestSOAPMessageContext(SOAPMessage message, boolean outbound) {

        this.message = message;
        properties = new HashMap<String, Object>();
        properties.put( MessageContext.MESSAGE_OUTBOUND_PROPERTY, outbound );
    }

    @SuppressWarnings("unused")
    public Object[] getHeaders(QName name, JAXBContext context, boolean required) {

        return null;
    }

    public SOAPMessage getMessage() {

        return message;
    }

    public Set<String> getRoles() {

        return null;
    }

    public void setMessage(SOAPMessage message) {

        this.message = message;
    }

    @SuppressWarnings("unused")
    public Scope getScope(String scope) {

        return null;
    }

    @SuppressWarnings("unused")
    public void setScope(String scopeName, Scope scope) {

        // empty
    }

    public void clear() {

    }

    @SuppressWarnings("unused")
    public boolean containsKey(Object key) {

        return false;
    }

    @SuppressWarnings("unused")
    public boolean containsValue(Object value) {

        return false;
    }

    public Set<java.util.Map.Entry<String, Object>> entrySet() {

        return null;
    }

    public Object get(Object key) {

        return properties.get( key );
    }

    public boolean isEmpty() {

        return false;
    }

    public Set<String> keySet() {

        return null;
    }

    public Object put(String key, Object value) {

        LOG.debug( "put: " + key );
        return properties.put( key, value );
    }

    @SuppressWarnings("unused")
    public void putAll(Map<? extends String, ? extends Object> t) {

    }

    @SuppressWarnings("unused")
    public Object remove(Object key) {

        return null;
    }

    public int size() {

        return 0;
    }

    public Collection<Object> values() {

        return null;
    }
}
