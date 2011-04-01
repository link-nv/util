package net.link.util.wicket.filter;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import net.link.util.lang.FallbackClassLoader;
import org.apache.wicket.protocol.http.WicketFilter;
import org.jetbrains.annotations.Nullable;


/**
 * Device {@link WicketFilter} used to set the correct classloader for device pages.
 */
public abstract class RemoteWicketFilter extends ProxiedWicketFilter {

    private static final ThreadLocal<HttpSession> httpSession = new ThreadLocal<HttpSession>();

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        httpSession.set( ((HttpServletRequest) request).getSession() );
        try {
            super.doFilter( request, response, chain );
        }
        finally {
            httpSession.remove();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ClassLoader getClassLoader() {

        return new FallbackClassLoader( getRemoteClassLoader( httpSession.get() ), super.getClassLoader() );
    }

    @Nullable
    protected abstract ClassLoader getRemoteClassLoader(HttpSession httpSession);
}
