package net.link.util.config;

import static com.google.common.base.Preconditions.*;
import static net.link.util.config.ConfigHolder.*;
import static net.link.util.util.TypeUtils.*;

import net.link.util.logging.Logger;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;


/**
 * <h2>{@link ConfigFilter}<br> <sub>A filter that provides some non-default {@link RootConfig} behaviour.</sub></h2>
 * <p/>
 * <p> Any application that wants {@link DefaultConfigFactory} to gain access to their web.xml for configuration parameters needs to use
 * this filter. The filter will make the servlet context available the default config from within the current thread, allowing it to gain
 * access to the context parameters contained within. </p>
 * <p/>
 * <p> The filter also provides a reliable way for an application to provide their own {@link RootConfig} and/or {@link AppConfig}
 * implementation. The filter will set the configs it was created with as active within the current thread for each request that goes
 * through it. </p>
 * <p/>
 * <p> <i>09 15, 2010</i> </p>
 *
 * @author lhunath
 */
public class ConfigFilter implements Filter {

    static final Logger logger = Logger.get( ConfigFilter.class );

    protected ConfigHolder   configHolder;
    protected ServletContext servletContext;

    /**
     * Create a config filter that requires the config holder to be specified in the servlet context via the {@code configHolder} init
     * parameter, or requires the root config class to be specified via the {@code configClass} init parameter.
     */
    public ConfigFilter() {

    }

    /**
     * @param configHolder The configuration holder that holds the config that requests coming through this filter should use.
     */
    protected ConfigFilter(ConfigHolder configHolder) {

        this.configHolder = configHolder;
    }

    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException {

        servletContext = filterConfig.getServletContext();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        ConfigHolder configHolder = getConfigHolder( request );
        logger.dbg( "[>>>] %s: %s @ %s", configHolder.getClass().getSimpleName(), servletContext.getServletContextName(),
                request instanceof HttpServletRequest? ((HttpServletRequest) request).getRequestURL(): null );

        try {
            setLocalConfigHolder( configHolder );
            for (DefaultConfigFactory factory : configHolder.getFactories()) {
                factory.setServletContext( servletContext );
                factory.setServletRequest( request );
            }

            chain.doFilter( request, response );
        }
        finally {
            for (DefaultConfigFactory factory : configHolder.getFactories()) {
                factory.unsetServletRequest();
                factory.unsetServletContext();
            }
            unsetLocalConfigHolder();

            logger.dbg( "[<<<] %s: %s", configHolder.getClass().getSimpleName(), servletContext.getServletContextName() );
        }
    }

    @Override
    public void destroy() {

        servletContext = null;
    }

    public ConfigHolder getConfigHolder(ServletRequest request) {

        if (configHolder == null)
            configHolder = loadConfigHolder( servletContext );

        return checkNotNull( configHolder );
    }

    @NotNull
    protected static ConfigHolder loadConfigHolder(ServletContext servletContext) {

        // Try to find a custom holder.
        String configHolder = servletContext.getInitParameter( "configHolder" );
        if (configHolder != null)
            return newInstance( configHolder );

        // Load the config factory.
        String configFactoryName = servletContext.getInitParameter( "configFactory" );
        DefaultConfigFactory configFactory = null;
        if (configFactoryName != null)
            configFactory = newInstance( configFactoryName );
        if (configFactory == null)
            configFactory = new DefaultConfigFactory( servletContext.getInitParameter( "configResource" ) );

        // Load the config class.
        String configClassName = checkNotNull( servletContext.getInitParameter( "configClass" ),
                "When using the standard ConfigFilter, either the configHolder or the configClass init parameter must be given." );
        Class<RootConfig> configClass = loadClass( configClassName );
        RootConfig configInstance = null;
        if (!configClass.isInterface())
            configInstance = newInstance( configClass );

        // Create the holder.
        return new ConfigHolder( configClass, configFactory, configInstance );
    }
}
