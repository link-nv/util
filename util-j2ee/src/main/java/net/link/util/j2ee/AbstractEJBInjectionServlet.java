/*
 * SafeOnline project.
 *
 * Copyright 2006-2010 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.j2ee;

import java.io.IOException;
import java.lang.reflect.Field;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.util.servlet.AbstractInjectionServlet;


/**
 * <h2>{@link AbstractEJBInjectionServlet}<br>
 * <sub>An {@link AbstractInjectionServlet} that also performs EJB injections.</sub></h2>
 *
 * <p>
 * <i>Jan 1, 2010</i>
 * </p>
 *
 * @author lhunath
 */
public abstract class AbstractEJBInjectionServlet extends AbstractInjectionServlet {

    @Override
    protected void doPostInvocation(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        injectEjbs();

        super.doPostInvocation( request, response );
    }

    @Override
    protected void doGetInvocation(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        injectEjbs();

        super.doGetInvocation( request, response );
    }

    protected abstract void injectEjbs()
            throws ServletException;
}
