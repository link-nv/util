package net.link.util.config;

/**
 * <p>This interface indicates the root of the configuration hierarchy.</p>
 * <p/>
 * <p>
 * <i>09 14, 2010</i>
 * </p>
 *
 * @author lhunath
 */
@Group(prefix = "")
public interface RootConfig {

    /**
     * @param appConfigType The extension's configuration class.
     *
     * @return The given extension's configuration.
     */

    <C extends AppConfig> C app(Class<C> appConfigType);
}
