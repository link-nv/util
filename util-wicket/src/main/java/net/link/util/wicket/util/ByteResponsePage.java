package net.link.util.wicket.util;

import java.io.IOException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.target.component.PageRequestTarget;


/**
 * <i>04 04, 2011</i>
 *
 * @author lhunath
 */
public class ByteResponsePage extends PageRequestTarget {

    public ByteResponsePage(final byte[] responseData) {

        super( new WebPage() {

            @Override
            protected void onRender(final MarkupStream markupStream) {

                try {
                    WicketUtils.getServletResponse().getOutputStream().write( responseData );
                }
                catch (IOException e) {
                    throw new RuntimeException( e );
                }
            }
        } );
    }
}
