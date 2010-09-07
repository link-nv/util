/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.j2ee;

import java.util.*;
import javax.ejb.EJBException;
import javax.naming.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Utils to ease the working with EJBs.
 *
 * @author fcorneli
 */
public class EJBUtils {

    private static final Log LOG = LogFactory.getLog( EJBUtils.class );

    private EJBUtils() {

        // empty
    }

    public static <E> E getEJB(Class<E> type) {

        InitialContext initialContext = getInitialContext();
        return getEJB( initialContext, type );
    }

    public static <E> E getEJB(String jndiName, Class<E> type) {

        InitialContext initialContext = getInitialContext();
        return getEJB( initialContext, jndiName, type );
    }

    public static <E> E getEJB(InitialContext initialContext, Class<E> type) {

        String jndiName = new FieldNamingStrategy().calculateName( type );
        return getEJB( initialContext, jndiName, type );
    }

    public static <E> E getEJB(InitialContext initialContext, String jndiName, Class<E> type) {

        try {
            LOG.debug( "ejb jndi lookup: " + jndiName );
            Object object = initialContext.lookup( jndiName );

            return type.cast( object );
        }

        catch (NamingException e) {
            throw new EJBException( "naming error for: " + jndiName, e );
        }
    }

    private static InitialContext getInitialContext() {

        try {
            return new InitialContext();
        }

        catch (NamingException e) {
            throw new RuntimeException( "naming error: " + e.getMessage(), e );
        }
    }

    public static <Type> List<Type> getComponents(InitialContext initialContext, String jndiPrefix, Class<Type> type) {

        LOG.debug( "get components at " + jndiPrefix );
        List<Type> components = new LinkedList<Type>();
        try {
            Context context;
            try {
                context = (Context) initialContext.lookup( jndiPrefix );
            } catch (NameNotFoundException e) {
                return components;
            }
            NamingEnumeration<NameClassPair> result = initialContext.list( jndiPrefix );
            while (result.hasMore()) {
                NameClassPair nameClassPair = result.next();
                String objectName = nameClassPair.getName();
                LOG.debug( objectName + ":" + nameClassPair.getClassName() );
                Object object = context.lookup( objectName );
                if (!type.isInstance( object )) {
                    String message =
                            "object \"" + jndiPrefix + "/" + objectName + "\" is not a " + type.getName() + "; it is " + (object == null
                                    ? "null": "a " + object.getClass().getName());
                    LOG.error( message );
                    throw new IllegalStateException( message );
                }
                Type component = type.cast( object );
                components.add( component );
            }
            return components;
        } catch (NamingException e) {
            throw new RuntimeException( "naming error: " + e.getMessage(), e );
        }
    }

    public static <Type> List<Type> getComponents(String jndiPrefix, Class<Type> type) {

        InitialContext initialContext;
        try {
            initialContext = new InitialContext();
        } catch (NamingException e) {
            throw new RuntimeException( "naming error: " + e.getMessage(), e );
        }
        return getComponents( initialContext, jndiPrefix, type );
    }

    public static <Type> Map<String, Type> getComponentNames(InitialContext initialContext, String jndiPrefix, Class<Type> type) {

        LOG.debug( "get component names at " + jndiPrefix );
        HashMap<String, Type> names = new HashMap<String, Type>();
        NamingEnumeration<NameClassPair> result;
        try {
            Context context;
            try {
                context = (Context) initialContext.lookup( jndiPrefix );
                result = initialContext.list( jndiPrefix );
            } catch (NameNotFoundException e) {
                return names;
            }

            while (result.hasMore()) {
                NameClassPair nameClassPair = result.next();
                String objectName = nameClassPair.getName();
                LOG.debug( objectName + ":" + nameClassPair.getClassName() );
                Object object = context.lookup( objectName );

                // If the bean is bound to a /local of the objectName.
                if (object instanceof Context) {
                    Context objectContext = (Context) object;
                    objectName += "/local";
                    object = objectContext.lookup( "local" );
                    LOG.debug( object.getClass().getName() );
                }

                // Check the object type.
                if (!type.isInstance( object )) {
                    String message = String.format( "object \"%s/%s\" is not a %s; it is a %s %s", jndiPrefix, objectName, type.getName(),
                            object == null? "null": "a " + object.getClass().getName(), object == null? "null": Arrays.asList(
                                    object.getClass().getInterfaces() ) );
                    LOG.error( message );
                    throw new IllegalStateException( message );
                }

                Type component = type.cast( object );
                names.put( objectName, component );
            }
            return names;
        } catch (NamingException e) {
            throw new RuntimeException( "naming error: " + e.getMessage(), e );
        }
    }

    public static <Type> Map<String, Type> getComponentNames(String jndiPrefix, Class<Type> type) {

        InitialContext initialContext;
        try {
            initialContext = new InitialContext();
        } catch (NamingException e) {
            throw new RuntimeException( "naming error: " + e.getMessage(), e );
        }
        return getComponentNames( initialContext, jndiPrefix, type );
    }

    public static void bindComponent(String jndiName, Object component)
            throws NamingException {

        LOG.debug( "bind component: " + jndiName );
        InitialContext initialContext = new InitialContext();
        String[] names = jndiName.split( "/" );
        Context context = initialContext;
        for (int idx = 0; idx < names.length - 1; idx++) {
            String name = names[idx];
            LOG.debug( "name: " + name );
            NamingEnumeration<NameClassPair> listContent = context.list( "" );
            boolean subContextPresent = false;
            while (listContent.hasMore()) {
                NameClassPair nameClassPair = listContent.next();
                if (!name.equals( nameClassPair.getName() ))
                    continue;
                subContextPresent = true;
            }
            if (!subContextPresent)
                context = context.createSubcontext( name );
            else
                context = (Context) context.lookup( name );
        }
        String name = names[names.length - 1];
        context.rebind( name, component );
    }

    public static void unbindComponent(String jndiName)
            throws NamingException {

        LOG.debug( "release component: " + jndiName );
        InitialContext initialContext = new InitialContext();
        String[] names = jndiName.split( "/" );
        Context context = initialContext;
        for (int idx = 0; idx < names.length - 1; idx++) {
            String name = names[idx];
            LOG.debug( "name: " + name );
            NamingEnumeration<NameClassPair> listContent = context.list( "" );
            boolean subContextPresent = false;
            while (listContent.hasMore()) {
                NameClassPair nameClassPair = listContent.next();
                if (!name.equals( nameClassPair.getName() ))
                    continue;
                subContextPresent = true;
            }
            if (!subContextPresent)
                return;
            else
                context = (Context) context.lookup( name );
        }
        String name = names[names.length - 1];
        context.unbind( name );
    }

    public static Object getComponent(String jndiName)
            throws NamingException {

        InitialContext initialContext = new InitialContext();
        return initialContext.lookup( jndiName );
    }
}
