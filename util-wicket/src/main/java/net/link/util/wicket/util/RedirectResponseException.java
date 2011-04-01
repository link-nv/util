/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.wicket.util;

import org.apache.wicket.*;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.target.component.PageRequestTarget;


/**
 * <h2>{@link RedirectResponseException}</h2>
 * <p/>
 * <p> [description / usage]. </p>
 * <p/>
 * <p> <i>Nov 18, 2008</i> </p>
 *
 * @author lhunath
 */
public class RedirectResponseException extends AbstractRestartResponseException {

    public RedirectResponseException(final RedirectResponse response) {

        RequestCycle rc = RequestCycle.get();
        if (rc == null)
            throw new IllegalStateException( "This exception can only be thrown from within request processing cycle" );

        Response r = rc.getResponse();
        if (!(r instanceof WebResponse))
            throw new IllegalStateException( "This exception can only be thrown when wicket is processing an http request" );

        // abort any further response processing
        rc.setRequestTarget( new PageRequestTarget( new WebPage() {

            @Override
            protected void onRender(final MarkupStream markupStream) {

                response.run();
            }
        } ) );
    }
}
