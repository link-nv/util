package net.link.util.wicket.util;

import java.io.IOException;
import net.link.util.servlet.BufferedServletResponseWrapper;
import org.apache.wicket.AbstractRestartResponseException;
import org.apache.wicket.RequestCycle;


/**
 * <i>04 04, 2011</i>
 *
 * @author lhunath
 */
public class StatelessResponseException extends AbstractRestartResponseException {

    public StatelessResponseException(final ServletResponse servletResponse) {

        try {
            BufferedServletResponseWrapper bufferedResponse = new BufferedServletResponseWrapper( WicketUtils.getServletResponse() );
            servletResponse.respond( WicketUtils.getServletRequest(), bufferedResponse );
            byte[] responseData = bufferedResponse.commitData();

            RequestCycle.get().setRequestTarget( new ByteResponsePage( responseData ) );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
}
