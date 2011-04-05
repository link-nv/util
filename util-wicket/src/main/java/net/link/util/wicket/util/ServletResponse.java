package net.link.util.wicket.util;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * <i>04 04, 2011</i>
 *
 * @author lhunath
 */
public interface ServletResponse {

    void respond(HttpServletRequest request, HttpServletResponse response)
            throws IOException;
}
