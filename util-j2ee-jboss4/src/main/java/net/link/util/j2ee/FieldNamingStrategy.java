/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.j2ee;

import net.link.util.logging.Logger;
import java.lang.reflect.Field;
import javax.ejb.EJB;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;


/**
 * <h2>{@link FieldNamingStrategy}<br>
 * <sub>Pull the JNDI binding of EJB service classes out of their class descriptions.</sub></h2>
 * <p/>
 * <p>
 * This injector assumes the field is of a type that is a bean interface with a publicly accessible JNDI_BINDING constant field which points
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

    private static final Logger logger = Logger.get( FieldNamingStrategy.class );

    public String calculateName(Class<?> ejbType) {

        try {
            if (ejbType.isAnnotationPresent( LocalBinding.class ))
                return ejbType.getAnnotation( LocalBinding.class ).jndiBinding();

            if (ejbType.isAnnotationPresent( RemoteBinding.class ))
                return ejbType.getAnnotation( RemoteBinding.class ).jndiBinding();

            return ejbType.getDeclaredField( "JNDI_BINDING" ).get( null ).toString();
        }

        catch (IllegalArgumentException e) {
            logger.err( e, "[BUG] Object is not the right type." );
        }
        catch (SecurityException e) {
            logger.err( e, "[BUG] Field injected not allowed." );
        }
        catch (IllegalAccessException e) {
            logger.err( e, "[BUG] Field injected not allowed." );
        }
        catch (NoSuchFieldException e) {
            logger.err( e, "[BUG] JNDI_BINDING field is not declared." );
        }

        throw new IllegalArgumentException( "Bean injection not supported for bean of type: " + ejbType );
    }

    public boolean isSupported(Field field) {

        return field.isAnnotationPresent( EJB.class );
    }
}
