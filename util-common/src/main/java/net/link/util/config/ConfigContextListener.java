package net.link.util.config;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.link.util.config.ConfigHolder.factory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <h2>{@link ConfigContextListener}<br> <sub>A servlet context listener that makes {@link RootConfig} available.</sub></h2>
 *
 * <p> Any application that wants {@link DefaultConfigFactory} to gain access to their web.xml for configuration parameters needs to use
 * this filter. The filter will make the servlet context available to the default config from within the current thread, allowing it to gain
 * access to the context parameters contained within. </p>
 *
 * <p> The context listener also provides a reliable way for an application to provide their own {@link RootConfig} and/or {@link AppConfig}
 * implementation. The context listener will set the configs it was created with as active within the current thread for servlet context
 * event that goes through it. </p>
 *
 * <p> <i>03 21, 2011</i> </p>
 *
 * @author lhunath
 */
public abstract class ConfigContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger( ConfigContextListener.class );

    private final ConfigHolder<?> configHolder;

    /**
     * @param configHolder The configuration holder that holds the config that requests coming through this filter should use.
     */
    protected ConfigContextListener(ConfigHolder<?> configHolder) {

        this.configHolder = checkNotNull( configHolder );
    }

    @Override
    public final void contextInitialized(final ServletContextEvent sce) {

        logger.debug( "Setting config context={}: configHolder={}", sce.getServletContext().getServletContextName(), configHolder );

        try {
            ConfigHolder.setLocalConfigHolder( configHolder );
            factory().setServletContext( sce.getServletContext() );

            doContextInitialized( sce );
        }
        finally {
            ConfigHolder.unsetLocalConfigHolder();
            logger.debug( "Unset config context={}: configHolder={}", sce.getServletContext().getServletContextName(), configHolder );
        }
    }

    @Override
    public final void contextDestroyed(final ServletContextEvent sce) {

        logger.debug( "Setting config context={}: configHolder={}", sce.getServletContext().getServletContextName(), configHolder );

        try {
            ConfigHolder.setLocalConfigHolder( configHolder );
            factory().setServletContext( sce.getServletContext() );

            doContextDestroyed( sce );
        }
        finally {
            ConfigHolder.unsetLocalConfigHolder();
            logger.debug( "Unset config context={}: configHolder={}", sce.getServletContext().getServletContextName(), configHolder );
        }
    }

    protected final ConfigHolder<?> getConfigHolder() {

        return configHolder;
    }

    protected abstract void doContextInitialized(ServletContextEvent sce);

    protected abstract void doContextDestroyed(ServletContextEvent sce);
}
