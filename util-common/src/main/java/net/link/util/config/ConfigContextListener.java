package net.link.util.config;

import static com.google.common.base.Preconditions.*;
import static net.link.util.config.ConfigHolder.*;

import com.lyndir.lhunath.opal.system.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jetbrains.annotations.NotNull;


/**
 * <h2>{@link ConfigContextListener}<br> <sub>A servlet context listener that makes {@link RootConfig} available.</sub></h2>
 * <p/>
 * <p> Any application that wants {@link DefaultConfigFactory} to gain access to their web.xml for configuration parameters needs to use
 * this filter. The filter will make the servlet context available to the default config from within the current thread, allowing it to
 * gain
 * access to the context parameters contained within. </p>
 * <p/>
 * <p> The context listener also provides a reliable way for an application to provide their own {@link RootConfig} and/or {@link
 * AppConfig}
 * implementation. The context listener will set the configs it was created with as active within the current thread for servlet context
 * event that goes through it. </p>
 * <p/>
 * <p> <i>03 21, 2011</i> </p>
 *
 * @author lhunath
 */
public abstract class ConfigContextListener implements ServletContextListener {

    static final Logger logger = Logger.get( ConfigContextListener.class );

    private ConfigHolder configHolder;

    /**
     * @param configHolder The configuration holder that holds the config that requests coming through this filter should use.  May be
     *                     {@code null}, in which case it requires the config holder to be specified in the servlet context via the
     *                     {@code configHolder} init parameter, or requires the root config class to be specified via the {@code
     *                     configClass} init parameter.
     */
    protected ConfigContextListener(ConfigHolder configHolder) {

        this.configHolder = configHolder;
    }

    @Override
    public final void contextInitialized(final ServletContextEvent sce) {

        ConfigHolder configHolder = getConfigHolder( sce );
        logger.dbg( "[>>>] %s: %s [init]", configHolder.getClass().getSimpleName(), sce.getServletContext().getServletContextName() );

        try {
            setLocalConfigHolder( configHolder );
            for (DefaultConfigFactory factory : factories()) {
                factory.setServletContext( sce.getServletContext() );
            }
            //            factory( DefaultConfigFactory.class ).setServletContext( sce.getServletContext() );

            doContextInitialized( sce );
        }
        finally {
            unsetLocalConfigHolder();
            logger.dbg( "[<<<] %s: %s", configHolder.getClass().getSimpleName(), sce.getServletContext().getServletContextName() );
        }
    }

    @Override
    public final void contextDestroyed(final ServletContextEvent sce) {

        ConfigHolder configHolder = getConfigHolder( sce );
        logger.dbg( "[>>>] %s: %s [destroy]", configHolder.getClass().getSimpleName(), sce.getServletContext().getServletContextName() );

        try {
            setLocalConfigHolder( configHolder );
            for (DefaultConfigFactory factory : factories()) {
                factory.setServletContext( sce.getServletContext() );
            }
            //            factory( DefaultConfigFactory.class ).setServletContext( sce.getServletContext() );

            doContextDestroyed( sce );
        }
        finally {
            unsetLocalConfigHolder();
            logger.dbg( "[<<<] %s: %s", configHolder.getClass().getSimpleName(), sce.getServletContext().getServletContextName() );
        }
    }

    @NotNull
    protected ConfigHolder getConfigHolder(final ServletContextEvent sce) {

        if (configHolder == null)
            configHolder = ConfigFilter.loadConfigHolder( sce.getServletContext() );

        return checkNotNull( configHolder );
    }

    protected abstract void doContextInitialized(ServletContextEvent sce);

    protected abstract void doContextDestroyed(ServletContextEvent sce);
}
