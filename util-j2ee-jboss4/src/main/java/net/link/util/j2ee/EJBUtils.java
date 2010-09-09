/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.j2ee;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;


/**
 * Utils to ease the working with EJBs.
 *
 * @author fcorneli
 */
public class EJBUtils {

    private static final Log LOG = LogFactory.getLog(EJBUtils.class);

    private EJBUtils() {

        // empty
    }

    public static <E> E getEJB(Class<E> type) {

        InitialContext initialContext = getInitialContext();
        return getEJB(initialContext, type);
    }

    public static <E> E getEJB(String jndiName, Class<E> type) {

        InitialContext initialContext = getInitialContext();
        return getEJB(initialContext, jndiName, type);
    }

    public static <E> E getEJB(InitialContext initialContext, Class<E> type) {

        String jndiName = new FieldNamingStrategy().calculateName(type);
        return getEJB(initialContext, jndiName, type);
    }

    public static <E> E getEJB(InitialContext initialContext, String jndiName, Class<E> type) {

        try {
            LOG.debug("ejb jndi lookup: " + jndiName);
            Object object = initialContext.lookup(jndiName);

            return type.cast(object);
        }

        catch (NamingException e) {
            throw new EJBException("naming error for: " + jndiName, e);
        }
    }

    private static InitialContext getInitialContext() {

        try {
            return new InitialContext();
        }

        catch (NamingException e) {
            throw new RuntimeException("naming error: " + e.getMessage(), e);
        }
    }
}
