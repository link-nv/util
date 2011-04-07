/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.j2ee;

import javax.ejb.EJBException;
import javax.naming.*;
import org.jetbrains.annotations.Nullable;


/**
 * Utils to ease the working with EJBs.
 *
 * @author fcorneli
 */
public abstract class EJBUtils {

    private static final NamingStrategy       NAMING_STRATEGY = new FieldNamingStrategy();
    private static final ThreadLocal<Context> INITIAL_CONTEXT = new ThreadLocal<Context>() {
        @Override
        protected Context initialValue() {

            try {
                return new InitialContext();
            }
            catch (NamingException e) {
                throw new RuntimeException( e );
            }
        }
    };

    public static <E> E getEJB(Class<E> type) {

        return getEJB( INITIAL_CONTEXT.get(), type );
    }

    public static <E> E getEJB(@Nullable String jndiName, Class<E> type) {

        return getEJB( INITIAL_CONTEXT.get(), jndiName, type );
    }

    public static <E> E getEJB(Context context, Class<E> type) {

        return getEJB( context, null, type );
    }

    public static <E> E getEJB(Context context, @Nullable String jndiName, Class<E> type) {

        if (jndiName == null)
            jndiName = NAMING_STRATEGY.calculateName( type );

        try {
            Object object = context.lookup( jndiName );

            return type.cast( object );
        }

        catch (NamingException e) {
            throw new EJBException( "Tried to look up bean: " + type + ", from JNDI(" + context.toString() + ") at: " + jndiName, e );
        }
    }
}
