package net.link.util.config;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.link.util.config.ConfigHolder.factory;

import java.io.IOException;
import javax.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <h2>{@link ConfigFilter}<br> <sub>A filter that provides some non-default {@link Config} behaviour.</sub></h2>
 *
 * <p> Any application that wants {@link DefaultConfigFactory} to gain access to their web.xml for configuration parameters needs to use
 * this filter. The filter will make the servlet context available the default config from within the current thread, allowing it to gain
 * access to the context parameters contained within. </p>
 *
 * <p> The filter also provides a reliable way for an application to provide their own {@link Config} and/or {@link AppConfig}
 * implementation. The filter will set the configs it was created with as active within the current thread for each request that goes
 * through it. </p>
 *
 * <p> <i>09 15, 2010</i> </p>
 *
 * @author lhunath
 */
public abstract class ConfigFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger( ConfigFilter.class );

    private final ConfigHolder<?> configHolder;

    private ServletContext servletContext;

    /**
     * @param configHolder The configuration holder that holds the config that requests coming through this filter should use.
     */
    protected ConfigFilter(ConfigHolder<?> configHolder) {

        this.configHolder = checkNotNull( configHolder );
    }

    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException {

        servletContext = filterConfig.getServletContext();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        logger.debug( "Setting config context={}: configHolder={}", servletContext.getServletContextName(), configHolder );

        try {
            ConfigHolder.setLocalConfigHolder( configHolder );
            factory().setServletContext( servletContext );
            factory().setServletRequest( request );

            chain.doFilter( request, response );
        }
        finally {
            factory().unsetServletRequest();
            factory().unsetServletContext();
            ConfigHolder.unsetLocalConfigHolder();
            logger.debug( "Unset config context={}: configHolder={}", servletContext.getServletContextName(), configHolder );
        }
    }

    @Override
    public void destroy() {

        servletContext = null;
    }

    protected final ConfigHolder<?> getConfigHolder() {

        return configHolder;
    }
}
