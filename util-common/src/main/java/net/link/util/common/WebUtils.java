package net.link.util.common;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;


/**
 * <h2>{@link WebUtils}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>02 04, 2011</i> </p>
 *
 * @author lhunath
 */
public abstract class WebUtils {

    @SuppressWarnings({ "unchecked" })
    public static Map<String, String[]> getParameterMap(final HttpServletRequest request) {

        return (Map<String, String[]>) request.getParameterMap();
    }
}
