package net.link.util.wicket.filter;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.util.servlet.HttpServletRequestEndpointWrapper;
import net.link.util.servlet.HttpServletResponseEndpointWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.protocol.http.WicketFilter;


/**
 * <h2>{@link ProxiedWicketFilter}<br> <sub>[in short] (TODO).</sub></h2>
 * <p/>
 * <p> <i>02 01, 2011</i> </p>
 *
 * @author lhunath
 */
public abstract class ProxiedWicketFilter extends WicketFilter {

    static final Log LOG = LogFactory.getLog( ProxiedWicketFilter.class );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        LOG.debug( "servlet " + getClass() + " beginning service" );
        String endpoint = getWrapperEndpoint( httpServletRequest );
        if (endpoint == null) {
            HttpServletRequestEndpointWrapper wrappedRequest = new HttpServletRequestEndpointWrapper( httpServletRequest, endpoint );
            HttpServletResponseEndpointWrapper wrappedResponse = new HttpServletResponseEndpointWrapper( httpServletResponse, endpoint );

            LOG.debug( "Wrapped request and response using endpoint: " + endpoint );
            super.doFilter( wrappedRequest, wrappedResponse, chain );
        } else {
            LOG.debug( "No endpoint defined.  Not wrapping request and response." );
            super.doFilter( httpServletRequest, httpServletResponse, chain );
        }
    }

    /**
     * <b>REQUEST AND RESPONSE WRAPPING</b>:
     * <p/>
     * <p> When we're behind a proxy or load balancer, the servlet request URI that the container gives us points to this machine rather
     * than the server that the request was actually sent to. This causes validation issues in OpenSAML and problems when redirecting to
     * relative URIs. </p>
     * <p/>
     * <p> <code>[User] >--[ https://linkid.be/app/foo ]--> [Proxy] >--[ http://provider.com/linkid/app/foo ]--> [linkID]</code> <br>
     * <code>[linkID: redirect to bar] >--[ redirect: http://provider.com/linkid/app/bar ]--> [Proxy] >--[ redirect:
     * http://provider.com/linkid/app/bar]--> [User]</code> <br> <code>[User] >--[ http://provider.com/linkid/app/bar ]-->
     * [Problem!]</code>
     * </p>
     * <p/>
     * <p> To solve this problem, we wrap the servlet request and response such that the request URI in the HttpServletRequest is the
     * request URI of the client's request (the request to the proxy/load balancer), and such that sendRedirects with relative URIs are
     * translated to absolute URIs using the client's request URI base. </p>
     *
     * @return The endpoint URL that the wrapper should use to replace the servlet request's requestURI and to calculate the absolute URL
     *         for the servlet response's relative sendRedirects.
     */
    protected abstract String getWrapperEndpoint(HttpServletRequest request);
}
