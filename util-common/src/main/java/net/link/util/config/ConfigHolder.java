package net.link.util.config;

import static com.google.common.base.Preconditions.*;

import com.google.common.collect.Maps;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <h2>{@link ConfigHolder}<br> <sub>[in short] (TODO).</sub></h2>
 * <p/>
 * <p> <i>09 17, 2010</i> </p>
 *
 * @author lhunath
 */
public class ConfigHolder {

    static final Logger logger = LoggerFactory.getLogger( ConfigHolder.class );

    private static final ThreadLocal<Boolean>      holderActivated = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {

            return false;
        }
    };
    private static final ThreadLocal<ConfigHolder> holder          = new ThreadLocal<ConfigHolder>() {
        @Override
        protected ConfigHolder initialValue() {

            if (globalConfigHolder != null)
                return globalConfigHolder;

            if (globalConfigHolderType != null)
                try {
                    return globalConfigHolderType.getConstructor().newInstance();
                }
                catch (InstantiationException e) {
                    throw new RuntimeException( e );
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException( e );
                }
                catch (NoSuchMethodException e) {
                    throw new RuntimeException( e );
                }
                catch (InvocationTargetException e) {
                    throw new RuntimeException( e );
                }

            return super.initialValue();
        }
    };

    private static Class<ConfigHolder> globalConfigHolderType;
    private static ConfigHolder        globalConfigHolder;

    private final Map<Class<? extends RootConfig>, RootConfig>           instances = Maps.newHashMap();
    private final Map<Class<? extends RootConfig>, DefaultConfigFactory> factories = Maps.newHashMap();

    //    private final Class<C>             configType;
    //    private final C                    config;
    //    private final DefaultConfigFactory defaultConfigFactory;

    /**
     * Call this method to globally set the type that will be used to instantiate new config holders when no specific config holder has
     * been
     * {@link #setLocalConfigHolder(ConfigHolder)}d.
     * <p/>
     * You could do this in a {@code static} block in your config holder implementation:
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
    protected static void setGlobalConfigHolderType(Class<ConfigHolder> globalConfigHolderType) {

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
     * You could do this in a {@code static} block in your config holder implementation:
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
    protected static void setGlobalConfigHolder(ConfigHolder globalConfigHolder) {

        holder.remove();
        ConfigHolder.globalConfigHolder = globalConfigHolder;
    }

    @SuppressWarnings({ "unchecked" })
    public static ConfigHolder get() {

        return checkNotNull( holder.get(),
                "No config holder set.  Set a global config holder or activate a local one (eg. using the ConfigFilter)." );
    }

    public static synchronized void setLocalConfigHolder(ConfigHolder instance) {

        checkState( !holderActivated.get(), "Tried to activate config holder: %s, but one is already active: %s", instance, holder.get() );
        checkNotNull( instance, "Tried to activate a config holder but none was given." );

        ConfigHolder.holderActivated.set( true );
        ConfigHolder.holder.set( instance );
    }

    public static synchronized void unsetLocalConfigHolder() {

        ConfigHolder.holder.remove();
        ConfigHolder.holderActivated.remove();
    }

    public static <C extends RootConfig> C config(Class<C> rootConfig) {

        return rootConfig.cast( get().getConfig( rootConfig ) );
    }

    public static Collection<DefaultConfigFactory> factories() {

        return get().getFactories();
        //        return factory.cast( get().getFactory() );
    }

    /**
     * Create a config holder that reads default configuration from the default resource "{@value
     * DefaultConfigFactory#DEFAULT_CONFIG_RESOURCE}}".
     *
     * @param configType The type of configuration that this holder provides.
     */
    public ConfigHolder(@NotNull final Class<? extends RootConfig> configType) {

        this( new DefaultConfigFactory(), configType, null );
    }

    /**
     * Create a config holder that holds the given configuration implementation.
     *
     * @param customConfig The configuration instance.
     */
    public ConfigHolder(@NotNull RootConfig customConfig) {

        this( new DefaultConfigFactory(), null, customConfig );
    }

    /**
     * Create a config holder.
     *
     * @param defaultConfigFactory The factory that creates default implementations of config classes.
     * @param configType           The type of configuration that this holder provides.
     * @param config               The configuration implementation to use.  May be {@code null}, in which case a default
     *                             implementation of configType is used as generated by defaultConfigFactory.
     *
     * @see DefaultConfigFactory The default configuration
     */
    protected <C extends RootConfig> ConfigHolder(@NotNull final DefaultConfigFactory defaultConfigFactory,
                                                  @Nullable final Class<C> configType, @Nullable C config) {

        add( defaultConfigFactory, configType, config );
    }

    public <C extends RootConfig> void add(@NotNull final DefaultConfigFactory defaultConfigFactory, @Nullable final Class<C> configType,
                                           @Nullable C config) {

        factories.put( configType, defaultConfigFactory );
        instances.put( configType, config );
    }

    public boolean hasConfig(Class<? extends RootConfig> configType) {

        return null != factories.get( configType ) || null != instances.get( configType );
    }

    /**
     * Provides the config held by this holder.
     *
     * @param configType the config type you want the instance for
     *
     * @return A configuration instance.
     */
    protected <C extends RootConfig> C getConfig(Class<C> configType) {

        C config = (C) instances.get( configType );
        DefaultConfigFactory defaultConfigFactory = factories.get( configType );

        // look at superclasses in maps
        if (null == config) {
            for (Class<? extends RootConfig> mapConfigType : instances.keySet()) {
                if (configType.isAssignableFrom( mapConfigType )) {
                    config = (C) instances.get( mapConfigType );
                }
            }
        }
        if (null == defaultConfigFactory) {
            for (Class<? extends RootConfig> mapConfigType : factories.keySet()) {
                if (configType.isAssignableFrom( mapConfigType )) {
                    defaultConfigFactory = factories.get( mapConfigType );
                }
            }
        }

        if (null == defaultConfigFactory) {
            defaultConfigFactory = new DefaultConfigFactory();
            factories.put( configType, defaultConfigFactory );
        }

        if (config != null)
            return defaultConfigFactory.getDefaultWrapper( config );

        return defaultConfigFactory.getDefaultImplementation(
                checkNotNull( configType, "No config implementation OR config type class set." ) );
    }

    /**
     * By default, this method only returns your root configuration interface.  If your holder will be used for application extensions, you
     * should return the extensions here, if possible.  It will allow searching through them for operations such as %{word} property value
     * expansions.
     *
     * @return All root configuration interfaces that this holder will used with.
     */
    protected Iterable<Class<? extends RootConfig>> getRootTypes() {

        return instances.keySet();
        //        return ImmutableSet.<Class<?>>of( getConfigType() );
    }

    /**
     * Provides the custom config held by this holder.
     *
     * @param configType the config type
     *
     * @return A custom configuration instance or {@code null} if there is no custom configuration set.
     */
    protected <C extends RootConfig> C getUnwrappedCustomConfig(Class<C> configType) {

        return (C) instances.get( configType );
    }

    /**
     * @return The factories that this config holder uses to create default implementations of config classes.
     */
    protected Collection<DefaultConfigFactory> getFactories() {

        return factories.values();
    }
}
