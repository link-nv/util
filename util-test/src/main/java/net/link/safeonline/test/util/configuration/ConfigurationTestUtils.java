/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util.configuration;

import java.lang.reflect.Field;
import net.link.safeonline.util.j2ee.Configurable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ConfigurationTestUtils {

    private static final Log LOG = LogFactory.getLog( ConfigurationTestUtils.class );

    public static void configure(Object target, String name, Object value)
            throws Exception {

        Field[] fields = target.getClass().getDeclaredFields();

        for (Field field : fields) {
            Configurable configurable = field.getAnnotation( Configurable.class );
            if (configurable != null) {
                field.setAccessible( true );
                if (configurable.name().equals( name ) || configurable.name().equals( "" ) && field.getName().equals( name )) {
                    LOG.debug( "setting field: " + name + " to: " + value );
                    field.set( target, value );
                }
            }
        }
    }
}
