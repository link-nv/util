package net.link.util.j2ee;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.*;
import java.util.*;

/**
 * Some utility methods for JNDI.
 */
public class JNDIUtils {

    private static final Log LOG = LogFactory.getLog(JNDIUtils.class);

    public static void bindComponent(String jndiName, Object component)
            throws NamingException {

        LOG.debug("bind component: " + jndiName);
        InitialContext initialContext = new InitialContext();
        String[] names = jndiName.split("/");
        Context context = initialContext;
        for (int idx = 0; idx < names.length - 1; idx++) {
            String name = names[idx];
            LOG.debug("name: " + name);
            NamingEnumeration<NameClassPair> listContent = context.list("");
            boolean subContextPresent = false;
            while (listContent.hasMore()) {
                NameClassPair nameClassPair = listContent.next();
                if (!name.equals(nameClassPair.getName()))
                    continue;
                subContextPresent = true;
            }
            if (!subContextPresent)
                context = context.createSubcontext(name);
            else
                context = (Context) context.lookup(name);
        }
        String name = names[names.length - 1];
        context.rebind(name, component);
    }

    public static void unbindComponent(String jndiName)
            throws NamingException {

        LOG.debug("release component: " + jndiName);
        InitialContext initialContext = new InitialContext();
        String[] names = jndiName.split("/");
        Context context = initialContext;
        for (int idx = 0; idx < names.length - 1; idx++) {
            String name = names[idx];
            LOG.debug("name: " + name);
            NamingEnumeration<NameClassPair> listContent = context.list("");
            boolean subContextPresent = false;
            while (listContent.hasMore()) {
                NameClassPair nameClassPair = listContent.next();
                if (!name.equals(nameClassPair.getName()))
                    continue;
                subContextPresent = true;
            }
            if (!subContextPresent)
                return;
            else
                context = (Context) context.lookup(name);
        }
        String name = names[names.length - 1];
        context.unbind(name);
    }

    public static Object getComponent(String jndiName)
            throws NamingException {

        InitialContext initialContext = new InitialContext();
        return initialContext.lookup(jndiName);
    }

    public static <Type> Map<String, Type> getComponentNames(String jndiPrefix, Class<Type> type) {

        InitialContext initialContext;
        try {
            initialContext = new InitialContext();
        } catch (NamingException e) {
            throw new RuntimeException("naming error: " + e.getMessage(), e);
        }
        return getComponentNames(initialContext, jndiPrefix, type);
    }

    public static <Type> Map<String, Type> getComponentNames(InitialContext initialContext, String jndiPrefix, Class<Type> type) {

        LOG.debug("get component names at " + jndiPrefix);
        HashMap<String, Type> names = new HashMap<String, Type>();
        NamingEnumeration<NameClassPair> result;
        try {
            Context context;
            try {
                context = (Context) initialContext.lookup(jndiPrefix);
                result = initialContext.list(jndiPrefix);
            } catch (NameNotFoundException e) {
                return names;
            }

            while (result.hasMore()) {
                NameClassPair nameClassPair = result.next();
                String objectName = nameClassPair.getName();
                LOG.debug(objectName + ":" + nameClassPair.getClassName());
                Object object = context.lookup(objectName);

                // If the bean is bound to a /local of the objectName.
                if (object instanceof Context) {
                    Context objectContext = (Context) object;
                    objectName += "/local";
                    object = objectContext.lookup("local");
                    LOG.debug(object.getClass().getName());
                }

                // Check the object type.
                if (!type.isInstance(object)) {
                    String message = String.format("object \"%s/%s\" is not a %s; it is a %s %s", jndiPrefix, objectName, type.getName(),
                            object == null ? "null" : "a " + object.getClass().getName(), object == null ? "null" : Arrays.asList(
                                    object.getClass().getInterfaces()));
                    LOG.error(message);
                    throw new IllegalStateException(message);
                }

                Type component = type.cast(object);
                names.put(objectName, component);
            }
            return names;
        } catch (NamingException e) {
            throw new RuntimeException("naming error: " + e.getMessage(), e);

        }
    }

    public static <T> List<T> getComponents(String jndiPrefix, Class<T> type) {

        InitialContext initialContext;
        try {
            initialContext = new InitialContext();
        } catch (NamingException e) {
            throw new RuntimeException("naming error: " + e.getMessage(), e);
        }
        return getComponents(initialContext, jndiPrefix, type);
    }

    public static <T> List<T> getComponents(InitialContext initialContext, String jndiPrefix, Class<T> type) {

        LOG.debug("get components at " + jndiPrefix);
        List<T> components = new LinkedList<T>();
        try {
            Context context;
            try {
                context = (Context) initialContext.lookup(jndiPrefix);
            } catch (NameNotFoundException e) {
                return components;
            }
            NamingEnumeration<NameClassPair> result = initialContext.list(jndiPrefix);
            while (result.hasMore()) {
                NameClassPair nameClassPair = result.next();
                String objectName = nameClassPair.getName();
                LOG.debug(objectName + ":" + nameClassPair.getClassName());
                Object object = context.lookup(objectName);
                if (!type.isInstance(object)) {
                    String message =
                            "object \"" + jndiPrefix + "/" + objectName + "\" is not a " + type.getName() + "; it is " + (object == null
                                    ? "null" : "a " + object.getClass().getName());
                    LOG.error(message);
                    throw new IllegalStateException(message);
                }
                T component = type.cast(object);
                components.add(component);
            }
            return components;
        } catch (NamingException e) {
            throw new RuntimeException("naming error: " + e.getMessage(), e);
        }
    }

}
