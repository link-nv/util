/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wicketstuff.javaee.injection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import javax.ejb.EJB;
import net.link.safeonline.util.j2ee.NamingStrategy;
import org.apache.wicket.injection.IFieldValueFactory;
import org.apache.wicket.proxy.IProxyTargetLocator;
import org.apache.wicket.proxy.LazyInitProxyFactory;
import org.wicketstuff.javaee.JavaEEBeanLocator;


/**
 * {@link IFieldValueFactory} that creates proxies of EJBs based on the {@link javax.ejb.EJB} annotation applied to a field.
 *
 * @author Filippo Diotalevi
 */
public class JavaEEProxyFieldValueFactory implements IFieldValueFactory {

    private final ConcurrentHashMap<IProxyTargetLocator, Object> cache = new ConcurrentHashMap<IProxyTargetLocator, Object>();
    private final NamingStrategy namingStrategy;

    /**
     * Constructor
     *
     * @param namingStrategy - naming strategy
     */
    public JavaEEProxyFieldValueFactory(NamingStrategy namingStrategy) {

        this.namingStrategy = namingStrategy;
    }

    /**
     * @see org.apache.wicket.injection.IFieldValueFactory#getFieldValue(java.lang.reflect.Field, java.lang.Object)
     */
    public Object getFieldValue(Field field, Object fieldOwner) {

        IProxyTargetLocator locator = getProxyTargetLocator( field );
        return getCachedProxy( field.getType(), locator );
    }

    /**
     * @see org.apache.wicket.injection.IFieldValueFactory#supportsField(java.lang.reflect.Field)
     */
    public boolean supportsField(Field field) {

        return field.isAnnotationPresent( EJB.class );
    }

    private Object getCachedProxy(Class<?> type, IProxyTargetLocator locator) {

        if (locator == null)
            return null;

        // if (cache.containsKey(locator))
        // return cache.get(locator);

        if (!Modifier.isFinal( type.getModifiers() )) {
            Object proxy = LazyInitProxyFactory.createProxy( type, locator );
            cache.put( locator, proxy );

            return proxy;
        }

        Object value = locator.locateProxyTarget();
        cache.put( locator, value );

        return value;
    }

    private IProxyTargetLocator getProxyTargetLocator(Field field) {

        if (field.isAnnotationPresent( EJB.class ))
            return new JavaEEBeanLocator( field.getType(), namingStrategy );

        return null;
    }
}
