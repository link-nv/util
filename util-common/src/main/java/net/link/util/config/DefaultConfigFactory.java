package net.link.util.config;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Splitter;
import com.google.common.collect.*;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import net.link.util.common.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <h2>{@link DefaultConfigFactory}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>09 14, 2010</i> </p>
 *
 * @author lhunath
 */
public class DefaultConfigFactory {

    private static final Logger logger = LoggerFactory.getLogger( DefaultConfigFactory.class );

    private static final String DEFAULT_CONFIG_RESOURCE = "config";
    private static final Pattern SYSTEM_PROPERTY = Pattern.compile( "\\$\\{([^\\}]*)\\}" );
    private static final Pattern LEADING_WHITESPACE = Pattern.compile( "^\\s+" );
    private static final Pattern TRAILING_WHITESPACE = Pattern.compile( "\\s+$" );
    private static final Pattern COMMA_DELIMITOR = Pattern.compile( "\\s*,\\s*" );

    private final ClassToInstanceMap<Object> proxyMap = MutableClassToInstanceMap.create();
    private final Map<Object, Object> wrapperMap = Maps.newHashMap();
    private final ThreadLocal<ServletContext> servletContext = new ThreadLocal<ServletContext>();
    private final ThreadLocal<ServletRequest> servletRequest = new ThreadLocal<ServletRequest>();
    private final ThreadLocal<Properties> properties = new ThreadLocal<Properties>() {
        @Override
        protected Properties initialValue() {

            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Properties properties = new Properties();

            // Load properties from XML property files.
            for (String configPath : getXMLResources()) {
                URL configUrl = contextClassLoader.getResource( configPath );
                if (configUrl != null)
                    try {
                        properties.loadFromXML( configUrl.openStream() );
                        logger.info( "Loaded config from: " + configUrl );
                        for (Map.Entry<Object, Object> config : properties.entrySet())
                            logger.info( String.format( "    - %-30s = %s", config.getKey(), config.getValue() ) );

                        return properties;
                    } catch (IOException e) {
                        logger.error( "While loading config from: " + configUrl, e );
                    }
            }

            // Load properties from plain property files.
            for (String configPath : getPlainResources()) {
                URL configUrl = contextClassLoader.getResource( configPath );
                if (configUrl != null)
                    try {
                        properties.load( configUrl.openStream() );
                        logger.info( "Loaded config from: " + configUrl );
                        for (Map.Entry<Object, Object> config : properties.entrySet())
                            logger.info( String.format( "    - %30s = %s", config.getKey(), config.getValue() ) );

                        return properties;
                    } catch (IOException e) {
                        logger.error( "While loading config from: " + configUrl, e );
                    }
            }

            // No properties files loaded.
            logger.debug( "No properties found." );
            return properties;
        }
    };

    private final String configResourceName;

    public DefaultConfigFactory() {

        this( DEFAULT_CONFIG_RESOURCE );
    }

    protected DefaultConfigFactory(String configResourceName) {

        this.configResourceName = configResourceName;
    }

    protected Iterable<String> getXMLResources() {

        return ImmutableList.of( configResourceName + ".xml", "../conf/" + configResourceName + ".xml",
                                 "../etc/" + configResourceName + ".xml" );
    }

    protected Iterable<String> getPlainResources() {

        return ImmutableList.of( configResourceName + ".properties", "../conf/" + configResourceName + ".properties",
                                 "../etc/" + configResourceName + ".properties" );
    }

    protected ServletContext getServletContext() {

        return servletContext.get();
    }

    public void setServletContext(ServletContext servletContext) {

        this.servletContext.set( servletContext );
    }

    public void unsetServletContext() {

        servletContext.remove();
    }

    protected ServletRequest getServletRequest() {

        return servletRequest.get();
    }

    public void setServletRequest(ServletRequest servletRequest) {

        this.servletRequest.set( servletRequest );
    }

    public void unsetServletRequest() {

        servletRequest.remove();
    }

    /**
     * Get the implementation for app config of the given type.
     *
     * Override this method to provide your own app config implementation.  By default, this method will use {@link
     * #getDefaultImplementation(Class)} to create a default implementation for the given app config.
     *
     * @param type The app configuration class that we should get an implementation for.
     * @param <A>  The type of the configuration class.
     *
     * @return An implementation of the configuration class.
     */
    protected <A extends AppConfig> A getAppImplementation(final Class<A> type) {

        return getDefaultImplementation( "", type );
    }

    /**
     * Get a default implementation for the given configuration class.
     *
     * @param type The configuration class that we should create a default implementation for.
     * @param <C>  The type of the configuration class.
     *
     * @return A default implementation of the configuration class.
     */
    public final <C> C getDefaultImplementation(final Class<C> type) {

        return getDefaultImplementation( "", type );
    }

    /**
     * Get a default implementation for the given configuration class.
     *
     * @param prefix The prefix that should go in front of all parameter names of properties provided by this configuration class.  It
     *               should <b>not</b> end with a dot.
     * @param type   The configuration class that we should create a default implementation for.
     * @param <C>    The type of the configuration class.
     *
     * @return A default implementation of the configuration class.
     */
    public final <C> C getDefaultImplementation(@NotNull String prefix, final Class<C> type) {

        // Check whether the type is a config group and determine the prefix to use for finding keys in this type.
        checkArgument( type.isInterface(), "Can't create a config proxy for %s, it is not an interface.", type );
        Config.Group configGroupAnnotation = checkNotNull( ObjectUtils.findAnnotation( type, Config.Group.class ),
                                                           "Can't create a config proxy for %s, it is not a config group (has no @Group).",
                                                           type );
        String proxyPrefix = prefix + configGroupAnnotation.prefix();
        if (!configGroupAnnotation.prefix().isEmpty())
            proxyPrefix += '.';

        // Get the proxy that will service this type.
        C proxy = proxyMap.getInstance( type );
        if (proxy == null)
            proxyMap.putInstance( type, proxy = type.cast( Proxy.newProxyInstance( type.getClassLoader(), new Class[] { type },
                                                                                   newDefaultImplementationHandler( proxyPrefix ) ) ) );

        return getDefaultWrapper( proxy );
    }

    /**
     * Get a wrapper for the given configuration implementation that tries to resolve <code>null</code> return values from the method's
     * annotations.
     *
     * @param config The configuration implementation that we should wrap.
     * @param <C>    The type of the configuration class.
     *
     * @return A wrapper for the given config implementation.
     */
    @SuppressWarnings({ "unchecked" })
    public final <C> C getDefaultWrapper(final C config) {

        // Check whether the type is a config group and determine the prefix to use for finding keys in this type.
        Config.Group configGroupAnnotation = checkNotNull( ObjectUtils.findAnnotation( config.getClass(), Config.Group.class ),
                                                           "Can't create a config wrapper for %s, it is not a config group (has no @Group).",
                                                           config.getClass() );

        // Get the proxy that will service this type.
        C wrapper = (C) wrapperMap.get( config );
        if (wrapper == null)
            wrapperMap.put( config,
                            wrapper = (C) Proxy.newProxyInstance( config.getClass().getClassLoader(), config.getClass().getInterfaces(),
                                                                  newDefaultWrapperHandler( config ) ) );

        return wrapper;
    }

    /**
     * Override this to provide your own implementation to provide values for methods invoked on the configuration group of the given
     * prefix.
     *
     * @param proxyPrefix The prefix identifies the configuration group on which the methods are invoked.
     *
     * @return An invocation handler that implements the logic which provides values when methods on the given prefix are invoked.
     */
    protected InvocationHandler newDefaultImplementationHandler(@NotNull String proxyPrefix) {

        return new DefaultImplementationHandler( proxyPrefix );
    }

    /**
     * Override this to provide your own implementation to provide default values for methods invoked on the configuration object where the
     * implementation handler couldn't resolve a value.
     *
     * @param config The configuration object which provides the normal values.  Invoke the handled method on this object to see if the
     *               implementation has a value to provide.  If the return value of this call is <code>null</code> , provide a default value
     *               for the method call.
     *
     * @return An invocation handler that implements the logic which provides default values when methods on the given config implementation
     *         yield no value.
     */
    protected InvocationHandler newDefaultWrapperHandler(Object config) {

        return new DefaultWrapperHandler( config );
    }

    /**
     * Resolve the value for a method invocation on a configuration object of the given prefix.
     *
     * @param prefix The prefix identifies the configuration group that the method was invoked on.  If not empty, it should end with a
     *               delimitor.
     * @param method The method identifies the configuration item that was requested.
     *
     * @return The value for the configuration item or <code>null</code>  if there is no value.  <b>Should be of the same type as the
     *         method's return type.</b>  See {@link #toType(String, Class)} for type conversion.
     */
    protected Object getValueFor(@NotNull String prefix, Method method) {

        String value = getProperty( prefix + method.getName() );

        return toType( value, method.getReturnType() );
    }

    /**
     * Resolve the default value for a method invocation on a configuration object.  This method is invoked by the default wrapper when it
     * detects that the configuration object's implementation failed to provide a value for the method call.  The standard behaviour is to
     * look at the method call's {@link Config.Property} annotation and get a default value out of it.  The call fails (with an unchecked
     * exception) if no default value is available and the method's annotation indicates that a value is required.
     *
     * @param method The method identifies the configuration item that was requested.
     *
     * @return The default value for the configuration item. <b>Should be of the same type as the method's return type .</b> See {@link
     *         #toType(String, Class)} for type conversion.
     */
    protected final Object getDefaultValueFor(Method method) {

        Config.Property propertyAnnotation = checkNotNull( method.getAnnotation( Config.Property.class ),
                                                           "Missing @Property on " + method );

        String value = findDefaultValueFor( method );
        if (propertyAnnotation.required())
            checkNotNull( value, "A required configuration property (for " + method + ") is unset." );

        return toType( value, method.getReturnType() );
    }

    /**
     * Resolve the default value for a method invocation on a configuration object.  This method is invoked by the default wrapper when it
     * detects that the configuration object's implementation failed to provide a value for the method call.  The standard behaviour is to
     * look at the method call's {@link Config.Property} annotation and get a default value out of it.
     *
     * @param method The method identifies the configuration item that was requested.
     *
     * @return The default value for the configuration item or <code>null</code>  if there is no default value.
     */
    protected String findDefaultValueFor(Method method) {

        Config.Property propertyAnnotation = checkNotNull( method.getAnnotation( Config.Property.class ),
                                                           "Missing @Property on " + method );

        String value = null;
        if (propertyAnnotation.unset().equals( Config.Property.AUTO ))
            value = generateValue( method );

        else if (!propertyAnnotation.unset().equals( Config.Property.NONE ))
            value = propertyAnnotation.unset();

        return value;
    }

    protected final String generateValue(final Method method) {

        String customGeneratedValue = generateValueExtension( method );
        if (customGeneratedValue != null)
            return customGeneratedValue;

        throw new UnsupportedOperationException( "Don't know how to generate value for method: " + method );
    }

    /**
     * Override this method to provide additional value generation strategies.  This method is invoked whenever the default implementation
     * can't generate a value for the given method.
     *
     * @param method The method for which to generate the return value.
     *
     * @return The generated value.
     */
    protected String generateValueExtension(final Method method) {

        return null;
    }

    /**
     * @param key The key that identifies the property.
     *
     * @return The value for the given property.
     */
    protected String getProperty(String key) {

        // logger.debug( "Getting property: {}", key );
        String value = null;
        if (properties != null && !properties.get().isEmpty())
            // First, try the properties, if set.
            value = properties.get().getProperty( key );
        // logger.debug( "Properties provided: {}", value );
        if (value == null)
            // Second, try the application's servlet context, if set.
            if (servletContext.get() != null) {
                value = servletContext.get().getInitParameter( key );
                // logger.debug( "Context provided: {}", value );
            }

        return filter( value );
    }

    protected final <T> T toType(String value, Class<T> type) {

        // Simple cases: null value & String type.
        if (value == null || type.isAssignableFrom( String.class ))
            return type.cast( value );

        // Reflection: type has a constructor that takes a string.
        try {
            return type.getConstructor( String.class ).newInstance( value );
        } catch (InstantiationException ignored) {
        } catch (IllegalAccessException ignored) {
        } catch (InvocationTargetException ignored) {
        } catch (NoSuchMethodException ignored) {
        }

        // Enums: use valueOf
        if (type.isEnum())
            return ObjectUtils.unsafeEnumValueOf( type, value );

        // Collections: Split the value
        if (Collection.class.isAssignableFrom( type )) {

            @SuppressWarnings({ "unchecked" })
            Class<? extends Collection> collectionType = (Class<? extends Collection>) type;
            String trimmedValue = LEADING_WHITESPACE.matcher( TRAILING_WHITESPACE.matcher( value ).replaceFirst( "" ) ).replaceFirst( "" );
            Iterable<String> splitValues = Splitter.on( COMMA_DELIMITOR ).split( trimmedValue );

            // In case type is a concrete collection
            try {
                @SuppressWarnings({ "unchecked" })
                Collection<String> values = collectionType.getConstructor().newInstance();
                Iterables.addAll( values, splitValues );
                return type.cast( values );
            } catch (InstantiationException ignored) {
            } catch (IllegalAccessException ignored) {
            } catch (NoSuchMethodException ignored) {
            } catch (InvocationTargetException ignored) {
            }

            // In case type is not instantiable
            if (type.isAssignableFrom( LinkedList.class ))
                return type.cast( Lists.newLinkedList( splitValues ) );
            if (type.isAssignableFrom( ArrayList.class ))
                return type.cast( Lists.newArrayList( splitValues ) );
            if (type.isAssignableFrom( LinkedHashSet.class ))
                return type.cast( Sets.newLinkedHashSet( splitValues ) );
            if (type.isAssignableFrom( HashSet.class ))
                return type.cast( Sets.newHashSet( splitValues ) );
            if (type.isAssignableFrom( TreeSet.class ))
                return type.cast( Sets.newTreeSet( splitValues ) );
        }

        T customToType = toTypeExtension( value, type );
        if (customToType != null)
            return customToType;

        throw new UnsupportedOperationException( "Don't know how to convert value: " + value + ", to the expected type: " + type );
    }

    /**
     * Override this method to provide additional type conversion strategies.  This method is invoked whenever the default implementation
     * can't convert the given value to the given type.
     *
     * @param value The value to convert.
     * @param type  The type to convert the value to.
     * @param <T>   The type to convert the value to.
     *
     * @return The value, converted to an instance of the given type.
     */
    protected <T> T toTypeExtension(final String value, final Class<T> type) {

        return null;
    }

    /**
     * Pass given data through a filter.
     *
     * The filter: - fills in system properties.
     *
     * @param value The data to filter.
     *
     * @return The given data, processed by the specified filter operations.
     */
    protected static String filter(String value) {

        if (value == null)
            return null;

        Map<Integer, Integer> ends = new TreeMap<Integer, Integer>();
        Map<Integer, String> replacements = new TreeMap<Integer, String>();

        Matcher matcher = SYSTEM_PROPERTY.matcher( value );
        while (matcher.find()) {
            String property = matcher.group( 1 );

            ends.put( matcher.start(), matcher.end() );
            replacements.put( matcher.start(), System.getProperty( property, "" ) );
        }

        SortedSet<Integer> reverseKeys = new TreeSet<Integer>( Collections.reverseOrder() );
        reverseKeys.addAll( replacements.keySet() );

        StringBuilder filteredData = new StringBuilder( value );
        for (Integer key : reverseKeys)
            filteredData.replace( key, ends.get( key ), replacements.get( key ) );

        // logger.debug( "Filtered to: {}", filteredData );
        return filteredData.toString();
    }

    @Override
    public String toString() {

        Class<?> type = getClass();
        while (type.isAnonymousClass())
            type = type.getSuperclass();

        return String.format( "%s#%x", type.getSimpleName(), hashCode() );
    }

    private class DefaultImplementationHandler implements InvocationHandler {

        private final String proxyPrefix;

        DefaultImplementationHandler(@NotNull String proxyPrefix) {

            this.proxyPrefix = proxyPrefix;
        }

        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {

            if (method.getDeclaringClass().equals( Object.class ))
                return method.invoke( this, args );

            if ("app".equals( method.getName() ) && method.getDeclaringClass().equals( Config.class ))
                //noinspection unchecked
                return getAppImplementation( (Class<AppConfig>) args[0] );

            Config.Group configGroupAnnotation = ObjectUtils.findAnnotation( method.getReturnType(), Config.Group.class );
            if (configGroupAnnotation != null)
                // Method return type is annotated with @Group, it's a config group: return a proxy for it.
                return getDefaultImplementation( proxyPrefix, method.getReturnType() );

            // Method does not return a config group; get its value.
            return getValueFor( proxyPrefix, method );
        }

        @Override
        public String toString() {

            return String.format( "%s.%s#%x", DefaultConfigFactory.this, getClass().getSimpleName(), hashCode() );
        }
    }


    private class DefaultWrapperHandler implements InvocationHandler {

        private final Object config;

        DefaultWrapperHandler(Object config) {

            this.config = config;
        }

        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {

            if (method.getDeclaringClass().equals( Object.class ))
                return method.invoke( this, args );

            Object value = method.invoke( config, args );

            if (value == null)
                value = getDefaultValueFor( method );

            if (ObjectUtils.findAnnotation( method.getReturnType(), Config.Group.class ) != null)
                // Method return type is annotated with @Group, it's a config group: wrap the value.
                value = getDefaultWrapper( value );

            logger.debug( String.format( "%s %s#%s = %s", //
                                         method.getReturnType().getSimpleName(), method.getDeclaringClass().getSimpleName(),
                                         method.getName(), value ) );
            return value;
        }

        @Override
        public String toString() {

            return String.format( "%s.%s#%x", DefaultConfigFactory.this, getClass().getSimpleName(), hashCode() );
        }
    }
}
