/*
 * SafeOnline project.
 *
 * Copyright 2006-2010 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.util;

import org.apache.wicket.*;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;


/**
 * <h2>{@link RedirectToPageException}</h2>
 * 
 * <p>
 * <i>Feb 5, 2010</i>
 * </p>
 * 
 * @author lhunath
 */
public class RedirectToPageException extends AbstractRestartResponseException {

    public RedirectToPageException(Class<? extends Page> pageClass) {

        this( pageClass, null );
    }

    public RedirectToPageException(Class<? extends Page> pageClass, PageParameters parameters) {

        RequestCycle rc = RequestCycle.get();
        if (rc == null)
            throw new IllegalStateException( "This exception can only be thrown from within request processing cycle" );
        Response r = rc.getResponse();
        if (!(r instanceof WebResponse))
            throw new IllegalStateException( "This exception can only be thrown when wicket is processing an http request" );

        // abort any further response processing
        rc.setRequestTarget( new RedirectRequestTarget( rc.urlFor( pageClass, parameters ).toString() ) );
    }
}
