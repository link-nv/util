/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.test.j2ee;

import com.lyndir.lhunath.lib.system.util.TypeUtils;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.Local;
import javax.naming.*;
import net.link.util.j2ee.NamingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.ejb3.annotation.LocalBinding;


/**
 * Utility class for JNDI unit testing.
 *
 * @author fcorneli
 */
public class JNDITestUtils {

    private static final Log LOG = LogFactory.getLog( JNDITestUtils.class );

    private Map<String, Object> components = new HashMap<String, Object>();

    private NamingStrategy namingStrategy;

    public void setNamingStrategy(NamingStrategy namingStrategy) {

        this.namingStrategy = namingStrategy;
    }

    /**
     * Bind a bean on JNDI using either its {@link LocalBinding} for the JNDI binding or the naming strategy to resolve it.
     *
     * @param beanClass The class that defines the binding to use for the bean. If the class has a {@link LocalBinding} annotation, that
     *                  will determine the JNDI binding. Otherwise, if the class is a {@link Local} interface, the preset {@link
     *                  NamingStrategy} will determine the binding to use.
     * @param bean      The bean object to register.
     */
    public void bindComponent(Class<?> beanClass, Object bean)
            throws NamingException {

        bindComponent( resolveJndiBinding( beanClass ), bean );
    }

    public void bindComponent(String jndiName, Object component)
            throws NamingException {

        LOG.debug( "bind component: " + jndiName );
        components.put( jndiName, component );

        InitialContext initialContext = new InitialContext();
        String[] names = jndiName.split( "/" );

        // Recursively create sub contexts.
        Context context = initialContext;
        for (int idx = 0; idx < names.length - 1; idx++) {
            String name = names[idx];
            LOG.debug( "name: " + name );
            NamingEnumeration<NameClassPair> contextContent = context.list( "" );

            boolean subContextPresent = false;
            while (contextContent.hasMore()) {
                NameClassPair nameClassPair = contextContent.next();

                if (name.equals( nameClassPair.getName() )) {
                    subContextPresent = true;
                    break;
                }
            }

            if (subContextPresent)
                context = (Context) context.lookup( name );
            else
                context = context.createSubcontext( name );
        }

        // Bind the component.
        String name = names[names.length - 1];
        context.rebind( name, component );
    }

    public void dump()
            throws NamingException {

        dump( new InitialContext(), 0 );
    }

    public void dump(Context context, int indent)
            throws NamingException {

        String indentString = String.format( "%" + (indent > 0? indent * 4: "") + "s", "" );

        System.err.format( "%s + (%s)\n", indentString, context.getNameInNamespace() );
        NamingEnumeration<Binding> contextContent = context.listBindings( "" );

        while (contextContent.hasMore()) {
            Binding binding = contextContent.next();

            System.err.format( "%s - %s: (%s) %s\n", indentString, binding.getName(), binding.getClassName(), binding.getObject() );
            if (binding.getObject() instanceof Context)
                dump( (Context) binding.getObject(), indent + 1 );
        }
        System.err.println();
    }

    /**
     * Look up a bean from JNDI by determining its JNDI binding using the set {@link NamingStrategy}.
     */
    public <S> S lookup(Class<S> serviceClass)
            throws NamingException {

        String jndiBinding = resolveJndiBinding( serviceClass );
        Object service = new InitialContext().lookup( jndiBinding );
        return serviceClass.cast( service );
    }

    public JNDITestUtils setUp() {

        System.setProperty( Context.INITIAL_CONTEXT_FACTORY, "org.shiftone.ooc.InitialContextFactoryImpl" );
        return this;
    }

    /**
     * Tear down the test JNDI tree. This will unbind all previously bound component.
     */
    public void tearDown()
            throws NamingException {

        InitialContext initialContext = new InitialContext();
        for (String name : components.keySet()) {
            LOG.debug( "unbinding: " + name );
            initialContext.unbind( name );
        }
    }

    private String resolveJndiBinding(Class<?> beanClass) {

        LocalBinding localBinding = TypeUtils.findAnnotation( beanClass, LocalBinding.class );
        if (localBinding != null)
            return localBinding.jndiBinding();
        else if (TypeUtils.hasAnnotation( beanClass, Local.class ) && namingStrategy != null)
            return namingStrategy.calculateName( beanClass );

        throw new IllegalArgumentException(
                "Could not determine the JNDI binding for: " + beanClass + " (has no LocalBinding or Local annotation?)" );
    }
}
