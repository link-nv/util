/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.util.j2ee;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;


/**
 * <h2>{@link FieldNamingStrategy}<br>
 * <sub>Pull the JNDI binding of EJB service classes out of their class descriptions.</sub></h2>
 *
 * <p>
 * This injector assumes the field is of a type that is a bean interface with a publicly accessible JNDI_BINDING constant field which points
 * to the JNDI location of the bean that needs to be injected into the field.
 * </p>
 *
 * <p>
 * <i>Sep 25, 2008</i>
 * </p>
 *
 * @author lhunath
 */
public class FieldNamingStrategy implements NamingStrategy {

    public String calculateName(Class<?> ejbType) {

        try {
            if (ejbType.isAnnotationPresent( LocalBinding.class ))
                return ejbType.getAnnotation( LocalBinding.class ).jndiBinding();

            if (ejbType.isAnnotationPresent( RemoteBinding.class ))
                return ejbType.getAnnotation( RemoteBinding.class ).jndiBinding();

            return ejbType.getDeclaredField( "JNDI_BINDING" ).get( null ).toString();
        }

        catch (IllegalArgumentException e) {
            getLog().error( "[BUG] Object is not the right type.", e );
        } catch (SecurityException e) {
            getLog().error( "[BUG] Field injected not allowed.", e );
        } catch (IllegalAccessException e) {
            getLog().error( "[BUG] Field injected not allowed.", e );
        } catch (NoSuchFieldException e) {
            getLog().error( "[BUG] JNDI_BINDING field is not declared.", e );
        }

        throw new IllegalArgumentException( "Bean injection not supported for bean of type: " + ejbType );
    }

    private Log getLog() {

        return LogFactory.getLog( getClass() );
    }
}
