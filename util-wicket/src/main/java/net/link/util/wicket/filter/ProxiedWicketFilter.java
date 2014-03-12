package net.link.util.wicket.filter;

import net.link.util.logging.Logger;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.util.servlet.HttpServletRequestEndpointWrapper;
import net.link.util.servlet.HttpServletResponseEndpointWrapper;
import org.apache.wicket.protocol.http.WicketFilter;
import org.jetbrains.annotations.Nullable;


/**
 * <b>REQUEST AND RESPONSE WRAPPING</b>:
 * <p/>
 * <p> When we're behind a proxy or load balancer, the servlet request's request URI is the request made by the proxy to the container,
 * not the request made by the user agent to the proxy.  This causes validation problems when the request is expected to come in on a
 * certain URL (eg. OpenSAML expects its tickets to go to the assertion consumer URL) and when the container resolves relative redirection
 * URLs based on the request URL (causing the container to make the relative redirection absolute against the internal URL, and then
 * giving that absolute URL to the user agent, who cannot reach it).  </p>
 * <code><pre>
 *     [User Agent]-->[ https://myapp.com/app/foo ]-->[Proxy]-->[ http://127.0.0.1/myapp/app/foo ]-->[MyApp Container]
 *     [MyApp: redirect to bar]-->[Container: redirect to http://127.0.0.1/myapp/app/bar ]-->[Proxy]-->[User Agent]
 *     [User Agent]-->[ http://127.0.0.1/myapp/app/bar ]-->[ Problem! ]
 * </pre></code>
 * <p> To solve this problem, we wrap the servlet request and response such that the request URI in the HttpServletRequest is the
 * request URI of the client's request (the request to the proxy/load balancer), and such that sendRedirects with relative URIs are
 * translated to absolute URIs using the client's request URI base.</p>
 *
 * @author lhunath
 */
public abstract class ProxiedWicketFilter extends WicketFilter {

    private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
    private static final Logger logger            = Logger.get( ProxiedWicketFilter.class );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        logger.dbg( "servlet " + getClass() + " beginning service" );
        String baseURL = getBaseFor( httpServletRequest );
        if (baseURL != null) {
            HttpServletRequestEndpointWrapper wrappedRequest = new HttpServletRequestEndpointWrapper( httpServletRequest, baseURL );
            HttpServletResponseEndpointWrapper wrappedResponse = new HttpServletResponseEndpointWrapper( wrappedRequest, httpServletResponse, baseURL );

            logger.dbg( "Wrapped request and response using baseURL: " + baseURL );
            super.doFilter( wrappedRequest, wrappedResponse, chain );
        } else {
            logger.dbg( "No baseURL defined.  Not wrapping request and response." );
            super.doFilter( httpServletRequest, httpServletResponse, chain );
        }
    }

    /**
     * The default implementation searches for the {@value ProxiedWicketFilter#X_FORWARDED_PROTO} header in the request.  This header can be
     * set by a proxy or load balancer to indicate that forwarding took place and what the original protocol was.  If the header is found,
     * the request will be wrapped using an endpoint based on {@link #getHTTPBase()} or {@link #getHTTPSBase()}, depending on what the
     * original request's protocol was.  The request's context path is appended to this base URL to form the final endpoint.  If the header
     * is not found, the default implementation of this method returns {@code null}, meaning no wrapping will take place.
     *
     * @return The endpoint URL that the wrapper should use to replace the servlet request's requestURI and to calculate the absolute URL
     * for the servlet response's relative sendRedirects.  Basically: Deployment URL + context path as seen by the user agent.
     */
    @Nullable
    protected String getBaseFor(HttpServletRequest request) {

        String forwardedProtocol = request.getHeader( X_FORWARDED_PROTO );
        if (forwardedProtocol != null) {
            if ("http".equalsIgnoreCase( forwardedProtocol ))
                return getHTTPBase();
            if ("https".equalsIgnoreCase( forwardedProtocol ))
                return getHTTPSBase();
        }

        return null;
    }

    protected abstract String getHTTPBase();

    protected abstract String getHTTPSBase();
}
