package net.link.util.config;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.InvocationTargetException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <h2>{@link ConfigHolder}<br> <sub>[in short] (TODO).</sub></h2>
 * <p/>
 * <p> <i>09 17, 2010</i> </p>
 *
 * @author lhunath
 */
public class ConfigHolder<C extends RootConfig> {

    private static final Logger logger = LoggerFactory.getLogger(ConfigHolder.class);
    private static final ThreadLocal<Boolean> holderActivated = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {

            return false;
        }
    };
    private static final ThreadLocal<ConfigHolder<?>> holder = new ThreadLocal<ConfigHolder<?>>() {
        @Override
        protected ConfigHolder<?> initialValue() {

            if (globalConfigHolder != null)
                return globalConfigHolder;

            if (globalConfigHolderType != null)
                try {
                    return globalConfigHolderType.getConstructor().newInstance();
                }
                catch (InstantiationException e) {
                    throw new RuntimeException(e);
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }

            return null;
        }
    };

    private static Class<ConfigHolder<?>> globalConfigHolderType;
    private static ConfigHolder<?> globalConfigHolder;

    private final Class<C> configType;
    private final C config;
    private final DefaultConfigFactory defaultConfigFactory;

    /**
     * Call this method to globally set the type that will be used to instantiate new config holders when no specific config holder has been
     * {@link #setLocalConfigHolder(ConfigHolder)}d.
     * <p/>
     * You could do this in a <code>static</code> block in your config holder implementation:
     * <p/>
     * <pre>
     * class MyConfigHolder extends ConfigHolder {
     *     static {
     *         setGlobalConfigHolderType(MyConfigHolder.class);
     *     }
     * }
     * </pre>
     *
     * @param globalConfigHolderType The type to instantiate when a config holder is needed but none is available.
     */
    protected static void setGlobalConfigHolderType(Class<ConfigHolder<?>> globalConfigHolderType) {

        holder.remove();
        ConfigHolder.globalConfigHolderType = globalConfigHolderType;
    }

    /**
     * Call this method to globally set the config holders when no specific config holder has been {@link
     * #setLocalConfigHolder(ConfigHolder)}d.
     * <p/>
     * It's a good idea to keep the config holder scoped to the thread.  To do that, use {@link #setGlobalConfigHolderType(Class)} instead.
     * The config holder set by this method has precedence to the one set by {@link #setGlobalConfigHolderType(Class)}.
     * <p/>
     * You could do this in a <code>static</code> block in your config holder implementation:
     * <p/>
     * <pre>
     * class MyConfigHolder extends ConfigHolder {
     *     static {
     *         setGlobalConfigHolder(new MyConfigHolder());
     *     }
     * }
     * </pre>
     *
     * @param globalConfigHolder The config holder to use when none is available.
     */
    protected static void setGlobalConfigHolder(ConfigHolder<?> globalConfigHolder) {

        holder.remove();
        ConfigHolder.globalConfigHolder = globalConfigHolder;
    }

    @SuppressWarnings({"unchecked"})
    public static <C extends RootConfig> ConfigHolder<C> get() {

        return checkNotNull( (ConfigHolder<C>) holder.get(),
                "No config holder set.  Set a global config holder or activate a local one (eg. using the ConfigFilter)." );
    }

    public static synchronized void setLocalConfigHolder(ConfigHolder<?> instance) {

        checkState( !holderActivated.get(), "Tried to activate config holder: %s, but one is already active: %s", instance, holder.get());
        checkNotNull( instance, "Tried to activate a config holder but none was given." );

        ConfigHolder.holderActivated.set(true);
        ConfigHolder.holder.set(instance);
    }

    public static synchronized void unsetLocalConfigHolder() {

        ConfigHolder.holder.remove();
        ConfigHolder.holderActivated.remove();
    }

    public static <C extends RootConfig> C config() {

        return ConfigHolder.<C>get().getConfig();
    }

    public static DefaultConfigFactory factory() {

        return get().getFactory();
    }

    /**
     * Create a config holder that reads default configuration from the default resource "{@value
     * DefaultConfigFactory#DEFAULT_CONFIG_RESOURCE}}".
     *
     * @param configType The type of configuration that this holder provides.
     */
    public ConfigHolder(final Class<C> configType) {

        this(new DefaultConfigFactory(), configType, null);
    }

    /**
     * Create a config holder that holds the given configuration implementation.
     *
     * @param customConfig The configuration instance.
     */
    public ConfigHolder(@NotNull C customConfig) {

        this(new DefaultConfigFactory(), null, customConfig);
    }

    /**
     * Create a config holder.
     *
     * @param defaultConfigFactory The factory that creates default implementations of config classes.
     * @param configType           The type of configuration that this holder provides.
     * @param config               The configuration implementation to use.  May be <code>null</code>, in which case a default
     *                             implementation of configType is used as generated by defaultConfigFactory.
     * @see DefaultConfigFactory The default configuration
     */
    protected ConfigHolder(final DefaultConfigFactory defaultConfigFactory, final Class<C> configType, C config) {

        this.defaultConfigFactory = defaultConfigFactory;
        this.configType = configType;
        this.config = config;
    }

    /**
     * Provides the config held by this holder.
     *
     * @return A configuration instance.
     */
    protected C getConfig() {

        if (config != null)
            return defaultConfigFactory.getDefaultWrapper(config);

        return defaultConfigFactory.getDefaultImplementation(
                checkNotNull(configType, "No config implementation OR config type class set."));
    }

    /**
     * Provides the custom config held by this holder.
     *
     * @return A custom configuration instance or <code>null</code> if there is no custom configuration set.
     */
    protected C getUnwrappedCustomConfig() {

        return config;
    }

    /**
     * @return The factory that this config holder uses to create default implementations of config classes.
     */
    protected DefaultConfigFactory getFactory() {

        return defaultConfigFactory;
    }
}
