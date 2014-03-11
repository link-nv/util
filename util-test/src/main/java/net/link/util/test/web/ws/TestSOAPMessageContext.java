/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.test.web.ws;

import java.util.*;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;


public class TestSOAPMessageContext implements SOAPMessageContext {

    private SOAPMessage message;

    private final Map<String, Object> properties;

    public TestSOAPMessageContext(SOAPMessage message, boolean outbound) {

        this.message = message;
        properties = new HashMap<String, Object>();
        properties.put( MessageContext.MESSAGE_OUTBOUND_PROPERTY, outbound );
    }

    @Override
    @SuppressWarnings("unused")
    public Object[] getHeaders(QName name, JAXBContext context, boolean required) {

        return null;
    }

    @Override
    public SOAPMessage getMessage() {

        return message;
    }

    @Override
    public Set<String> getRoles() {

        return null;
    }

    @Override
    public void setMessage(SOAPMessage message) {

        this.message = message;
    }

    @Override
    @SuppressWarnings("unused")
    public Scope getScope(String scope) {

        return null;
    }

    @Override
    @SuppressWarnings("unused")
    public void setScope(String scopeName, Scope scope) {

        // empty
    }

    @Override
    public void clear() {

    }

    @Override
    @SuppressWarnings("unused")
    public boolean containsKey(Object key) {

        return false;
    }

    @Override
    @SuppressWarnings("unused")
    public boolean containsValue(Object value) {

        return false;
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {

        return null;
    }

    @Override
    public Object get(Object key) {

        return properties.get( key );
    }

    @Override
    public boolean isEmpty() {

        return false;
    }

    @Override
    public Set<String> keySet() {

        return null;
    }

    @Override
    public Object put(String key, Object value) {

        return properties.put( key, value );
    }

    @Override
    @SuppressWarnings("unused")
    public void putAll(Map<? extends String, ? extends Object> t) {

    }

    @Override
    @SuppressWarnings("unused")
    public Object remove(Object key) {

        return null;
    }

    @Override
    public int size() {

        return 0;
    }

    @Override
    public Collection<Object> values() {

        return null;
    }
}
