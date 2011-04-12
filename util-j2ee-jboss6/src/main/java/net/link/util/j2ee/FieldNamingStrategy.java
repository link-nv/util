/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.j2ee;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.*;
import com.lyndir.lhunath.lib.system.logging.Logger;
import com.lyndir.lhunath.lib.system.util.ObjectUtils;
import com.lyndir.lhunath.lib.system.util.TypeUtils;
import java.lang.reflect.Field;
import javax.ejb.EJB;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;


/**
 * <h2>{@link FieldNamingStrategy}<br>
 * <sub>Pull the JNDI binding of EJB service classes out of their class descriptions.</sub></h2>
 * <p/>
 * <p>
 * This injector assumes the field is of a type that is a bean interface with a publicly accessible JNDI_BINDING constant field which
 * points
 * to the JNDI location of the bean that needs to be injected into the field.
 * </p>
 * <p/>
 * <p>
 * <i>Sep 25, 2008</i>
 * </p>
 *
 * @author lhunath
 */
public class FieldNamingStrategy implements NamingStrategy {

    static final Logger logger = Logger.get( FieldNamingStrategy.class );

    private static final Function<TypeUtils.LastResult<Class<?>, String>, String> findJNDIBindingFunction = new Function<TypeUtils.LastResult<Class<?>, String>, String>() {
        @Override
        public String apply(final TypeUtils.LastResult<Class<?>, String> from) {

            return ObjectUtils.getOrDefault( from.getLastResult(), new Supplier<String>() {
                @Override
                public String get() {

                    try {
                        Field jndiBinding = from.getCurrent().getDeclaredField( "JNDI_BINDING" );
                        if (jndiBinding == null)
                            return null;

                        return jndiBinding.get( null ).toString();
                    }

                    catch (NoSuchFieldException ignored) {
                        return null;
                    }
                    catch (IllegalAccessException e) {
                        throw Throwables.propagate( e );
                    }
                }
            } );
        }
    };

    @Override
    public String calculateName(Class<?> ejbType) {

        LocalBinding localBinding = TypeUtils.findAnnotation( ejbType, LocalBinding.class );
        if (localBinding != null)
            return localBinding.jndiBinding();

        RemoteBinding remoteBinding = TypeUtils.findAnnotation( ejbType, RemoteBinding.class );
        if (remoteBinding != null)
            return remoteBinding.jndiBinding();

        return checkNotNull( TypeUtils.forEachSuperTypeOf( ejbType, findJNDIBindingFunction, findJNDIBindingFunction ),
                "JNDI Binding not found for type: " + ejbType + "(super: " + ejbType.getSuperclass() + "), " //
                + "interfaces: " + ObjectUtils.describe( ejbType.getInterfaces() ) );
    }

    public boolean isSupported(Field field) {

        return field.isAnnotationPresent( EJB.class );
    }

    private Log getLog() {

        return LogFactory.getLog( getClass() );
    }
}
