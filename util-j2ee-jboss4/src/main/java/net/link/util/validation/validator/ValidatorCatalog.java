/*
 * SafeOnline project.
 *
 * Copyright 2005-2007 Frank Cornelis H.S.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.validation.validator;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;


/**
 * Catalog for validators. Flyweight pattern.
 *
 * @author fcorneli
 */
public class ValidatorCatalog {

    @SuppressWarnings("unchecked")
    private static Map<Class<? extends Validator<? extends Annotation>>, Validator<? extends Annotation>> instances = new HashMap<Class<? extends Validator<? extends Annotation>>, Validator<? extends Annotation>>();

    private ValidatorCatalog() {

        // empty
    }

    public static <A extends Annotation, V extends Validator<A>> V getInstance(Class<V> validatorClass) {

        V instance = validatorClass.cast( ValidatorCatalog.instances.get( validatorClass ) );
        if (null == instance)
            try {
                instance = validatorClass.newInstance();
                ValidatorCatalog.instances.put( validatorClass, instance );
            } catch (Exception e) {
                throw new IllegalStateException( "Unable to get instance of class: " + validatorClass.getName() );
            }

        return instance;
    }
}
