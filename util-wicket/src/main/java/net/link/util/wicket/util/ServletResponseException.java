package net.link.util.wicket.util;

import java.io.IOException;
import org.apache.wicket.*;


/**
 * <i>04 04, 2011</i>
 *
 * @author lhunath
 */
public class ServletResponseException extends AbstractRestartResponseException {

    public ServletResponseException(final ServletResponse servletResponse) {

        RequestCycle.get().setRequestTarget( new IRequestTarget() {
            @Override
            public void respond(final RequestCycle requestCycle) {

                try {
                    servletResponse.respond( WicketUtils.getServletRequest( requestCycle ),
                            WicketUtils.getServletResponse( requestCycle ) );
                }
                catch (IOException e) {
                    throw new RuntimeException( e );
                }
            }

            @Override
            public void detach(final RequestCycle requestCycle) {

            }
        } );
    }
}
