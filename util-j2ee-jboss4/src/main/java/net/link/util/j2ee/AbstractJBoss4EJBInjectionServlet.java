/*
 * SafeOnline project.
 *
 * Copyright 2006-2010 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.j2ee;

import net.link.util.logging.Logger;
import java.lang.reflect.Field;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import net.link.util.servlet.AbstractInjectionServlet;


/**
 * <h2>{@link AbstractEJBInjectionServlet}<br>
 * <sub>An {@link AbstractInjectionServlet} that also performs {@link EJB} injections.</sub></h2>
 * <p/>
 * <p>
 * <i>Jan 1, 2010</i>
 * </p>
 *
 * @author lhunath
 */
public abstract class AbstractJBoss4EJBInjectionServlet extends AbstractEJBInjectionServlet {

    private static final Logger logger = Logger.get( AbstractJBoss4EJBInjectionServlet.class );

    protected void injectEjbs()
            throws ServletException {

        for (Class<?> type = getClass(); type != Object.class; type = type.getSuperclass()) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                EJB ejb = field.getAnnotation( EJB.class );
                if (null == ejb)
                    continue;
                String mappedName = ejb.mappedName();
                Class<?> fieldType = field.getType();
                if (false == fieldType.isInterface())
                    throw new ServletException( String.format( "field %s.%s's type should be an interface", getClass(), field ) );
                if (mappedName == null || mappedName.length() == 0)
                    mappedName = new FieldNamingStrategy().calculateName( fieldType );
                if (mappedName == null || mappedName.length() == 0)
                    throw new ServletException( String.format( "field %s.%s's @EJB requires mappedName attribute", getClass(), field ) );
                logger.dbg( "injecting: " + mappedName );

                try {
                    Object ejbRef = EJBUtils.getEJB( mappedName, fieldType );
                    field.setAccessible( true );
                    try {
                        field.set( this, ejbRef );
                    }
                    catch (IllegalArgumentException e) {
                        throw new ServletException( String.format( "while injecting into %s:", getClass() ), e );
                    }
                    catch (IllegalAccessException e) {
                        throw new ServletException( String.format( "while injecting into %s:", getClass() ), e );
                    }
                }
                catch (RuntimeException e) {
                    throw new ServletException( String.format( "couldn't resolve EJB named: %s (while injecting into %s)", mappedName, getClass() ), e );
                }
            }
        }
    }
}
