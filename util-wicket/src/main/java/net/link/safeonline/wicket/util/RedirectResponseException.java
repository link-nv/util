/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.util;

import org.apache.wicket.AbstractRestartResponseException;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebResponse;


/**
 * <h2>{@link RedirectResponseException}</h2>
 *
 * <p>
 * [description / usage].
 * </p>
 *
 * <p>
 * <i>Nov 18, 2008</i>
 * </p>
 *
 * @author lhunath
 */
public class RedirectResponseException extends AbstractRestartResponseException {

    public RedirectResponseException(IRequestTarget target) {

        RequestCycle rc = RequestCycle.get();
        if (rc == null)
            throw new IllegalStateException( "This exception can only be thrown from within request processing cycle" );

        Response r = rc.getResponse();
        if (!(r instanceof WebResponse))
            throw new IllegalStateException( "This exception can only be thrown when wicket is processing an http request" );

        // abort any further response processing
        rc.setRequestTarget( target );
    }
}
