package net.link.util.config;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Splitter;
import com.google.common.collect.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <h2>{@link DefaultConfigFactory}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>09 14, 2010</i> </p>
 *
 * @author lhunath
 */
public class DefaultConfigFactory<A extends AppConfig> {

    private static final Logger logger = LoggerFactory.getLogger( DefaultConfigFactory.class );

    private static final String  DEFAULT_CONFIG_RESOURCE = "config";
    private static final Pattern SYSTEM_PROPERTY         = Pattern.compile( "\\$\\{([^\\}]*)\\}" );
    private static final Pattern LEADING_WHITESPACE      = Pattern.compile( "^\\s+" );
    private static final Pattern TRAILING_WHITESPACE     = Pattern.compile( "\\s+$" );
    private static final Pattern COMMA_DELIMITOR         = Pattern.compile( "\\s*,\\s*" );

    private final ClassToInstanceMap<Object>  proxyMap       = MutableClassToInstanceMap.create();
    private final Map<Object, Object>         wrapperMap     = Maps.newHashMap();
    private final ThreadLocal<ServletContext> servletContext = new ThreadLocal<ServletContext>();
    private final ThreadLocal<ServletRequest> servletRequest = new ThreadLocal<ServletRequest>();
    private final ThreadLocal<Properties>     properties     = new ThreadLocal<Properties>() {
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

    private final String   configResourceName;
    private final Class<A> appConfigType;

    public DefaultConfigFactory() {

        this( null );
    }

    public DefaultConfigFactory(Class<A> appConfig) {

        this( DEFAULT_CONFIG_RESOURCE, appConfig );
    }

    protected DefaultConfigFactory(String configResourceName, Class<A> appConfigType) {

        this.configResourceName = configResourceName;
        this.appConfigType = appConfigType;
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

        return this.servletContext.get();
    }

    public void setServletContext(ServletContext servletContext) {

        this.servletContext.set( servletContext );
    }

    public void unsetServletContext() {

        servletContext.remove();
    }

    protected ServletRequest getServletRequest() {

        return this.servletRequest.get();
    }

    public void setServletRequest(ServletRequest servletRequest) {

        this.servletRequest.set( servletRequest );
    }

    public void unsetServletRequest() {

        servletRequest.remove();
    }

    /**
     * Get a default implementation for the given configuration class.
     *
     * @param type The configuration class that we should create a default implementation for.
     * @param <C>  The type of the configuration class.
     *
     * @return A default implementation of the configuration class.
     */
    public <C> C getDefaultImplementation(final Class<C> type) {

        return getDefaultImplementation( null, type );
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
    public <C> C getDefaultImplementation(String prefix, final Class<C> type) {

        // Check whether the type is a config group and determine the prefix to use for finding keys in this type.
        Config.Group configGroupAnnotation = checkNotNull( findAnnotation( type, Config.Group.class ),
                "Can't create a config proxy for %s, it is not a config group (has no @Group).", type );
        checkArgument( type.isInterface(), "Can't create a config proxy for %s, it is not an interface." );
        final String proxyPrefix = (prefix != null && prefix.length() > 0? prefix + '.': "") + configGroupAnnotation.prefix();

        // Get the proxy that will service this type.
        C proxy = proxyMap.getInstance( type );
        if (proxy == null)
            proxyMap.putInstance( type, proxy = type.cast( Proxy.newProxyInstance( type.getClassLoader(), new Class[] { type },
                    new DefaultImplementationHandler( proxyPrefix ) ) ) );

        return getDefaultWrapper( proxy );
    }


    /**
     * Get a wrapper for the given configuration implementation that tries to resolve <code>null</code> return values from the method's annotations.
     *
     * @param config The configuration implementation that we should wrap.
     * @param <C>    The type of the configuration class.
     *
     * @return A wrapper for the given config implementation.
     */
    @SuppressWarnings( { "unchecked" })
    public <C> C getDefaultWrapper(final C config) {

        // Check whether the type is a config group and determine the prefix to use for finding keys in this type.
        Config.Group configGroupAnnotation = checkNotNull( findAnnotation( config.getClass(), Config.Group.class ),
                "Can't create a config wrapper for %s, it is not a config group (has no @Group).", config.getClass() );

        // Get the proxy that will service this type.
        C wrapper = (C) wrapperMap.get( config );
        if (wrapper == null)
            wrapperMap.put( config, wrapper = (C) Proxy.newProxyInstance( config.getClass().getClassLoader(),
                    config.getClass().getInterfaces(), new DefaultWrapperHandler( config ) ) );

        return wrapper;
    }

    /**
     * Recursively search a type's inheritance hierarchy for an annotation.
     *
     * @param type           The class whose hierarchy to search.
     * @param annotationType The annotation type to search for.
     * @param <A>            The annotation type.
     *
     * @return The annotation of the given annotation type in the given type's hierarchy or <code>null</code> if the type's hierarchy contains no classes that have the given annotation type set.
     */
    private <A extends Annotation> A findAnnotation(Class<?> type, Class<A> annotationType) {

        A annotation = type.getAnnotation( annotationType );
        if (annotation != null)
            return annotation;

        for (Class<?> subType : type.getInterfaces()) {
            annotation = findAnnotation( subType, annotationType );
            if (annotation != null)
                return annotation;
        }
        if (type.getSuperclass() != null) {
            annotation = findAnnotation( type.getSuperclass(), annotationType );
            if (annotation != null)
                return annotation;
        }

        return null;
    }

    private Object getPropertyValueFor(String prefix, Method method) {

        Config.Property propertyAnnotation = checkNotNull( method.getAnnotation( Config.Property.class ),
                "Missing @Property on " + method );

        String key = (prefix != null && prefix.length() > 0? prefix + '.': "") + method.getName();
        String value = getProperty( key );

        return _toType( value, method.getReturnType(), propertyAnnotation );
    }

    private Object getAnnotationValueFor(Method method) {

        Config.Property propertyAnnotation = checkNotNull( method.getAnnotation( Config.Property.class ),
                "Missing @Property on " + method );

        String value = null;
        if (propertyAnnotation.unset().equals( Config.Property.AUTO ))
            value = _generateValue( method );

        else if (!propertyAnnotation.unset().equals( Config.Property.NONE ))
            value = propertyAnnotation.unset();

        if (propertyAnnotation.required())
            checkNotNull( value, "A required configuration property (for " + method + ") is unset." );

        return _toType( value, method.getReturnType(), propertyAnnotation );
    }

    protected A getAppConfig() {

        return getDefaultImplementation( checkNotNull( appConfigType, "Can't get app config, app config type was not set." ) );
    }

    private String _generateValue(final Method method) {

        String customGeneratedValue = generateValue( method );
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
    protected String generateValue(final Method method) {

        return null;
    }

    /**
     * @param key The key that identifies the property.
     *
     * @return The value for the given property.
     */
    protected String getProperty(String key) {

        String value = null;
        if (properties != null && !properties.get().isEmpty())
            // First, try the properties, if set.
            value = properties.get().getProperty( key );
        if (value == null)
            // Second, try the application's servlet context, if set.
            if (servletContext.get() != null)
                value = servletContext.get().getInitParameter( key );

        return filter( value );
    }

    private <T> T _toType(String value, Class<T> type, Config.Property propertyAnnotation) {

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
            return unsafeEnumValueOf( type, value );

        // Collections: Split the value
        if (Collection.class.isAssignableFrom( type )) {

            @SuppressWarnings( { "unchecked" })
            Class<? extends Collection> collectionType = (Class<? extends Collection>) type;
            String trimmedValue = LEADING_WHITESPACE.matcher( TRAILING_WHITESPACE.matcher( value ).replaceFirst( "" ) ).replaceFirst( "" );
            Iterable<String> splitValues = Splitter.on( COMMA_DELIMITOR ).split( trimmedValue );

            // In case type is a concrete collection
            try {
                @SuppressWarnings( { "unchecked" })
                Collection<String> values = collectionType.newInstance();
                Iterables.addAll( values, splitValues );
                return type.cast( values );
            } catch (InstantiationException ignored) {
            } catch (IllegalAccessException ignored) {
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

        T customToType = toType( value, type, propertyAnnotation );
        if (customToType != null)
            return customToType;

        throw new UnsupportedOperationException( "Don't know how to convert value: " + value + ", to the expected type: " + type );
    }

    /**
     * Override this method to provide additional type conversion strategies.  This method is invoked whenever the default implementation
     * can't convert the given value to the given type.
     *
     * @param value              The value to convert.
     * @param type               The type to convert the value to.
     * @param propertyAnnotation The annotation that is present on the configuration method for which we're generating a return value.
     * @param <T>                The type to convert the value to.
     *
     * @return The value, converted to an instance of the given type.
     */
    protected <T> T toType(final String value, final Class<T> type, final Config.Property propertyAnnotation) {

        return null;
    }

    @SuppressWarnings( { "unchecked" })
    private static <T> T unsafeEnumValueOf(Class<T> type, String value) {

        return type.cast( Enum.valueOf( (Class<Enum>) type, value ) );
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

        StringBuffer filteredData = new StringBuffer( value );
        for (Integer key : reverseKeys)
            filteredData.replace( key, ends.get( key ), replacements.get( key ) );

        return filteredData.toString();
    }

    @Override
    public String toString() {

        Class<?> type = getClass();
        while (type.isAnonymousClass())
            type = type.getSuperclass();

        return String.format("%s#%x", type.getSimpleName(), hashCode());
    }

    private class DefaultImplementationHandler implements InvocationHandler {

        private final String proxyPrefix;

        public DefaultImplementationHandler(String proxyPrefix) {

            this.proxyPrefix = proxyPrefix;
        }

        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {

            if (method.getDeclaringClass().equals( Object.class ))
                return method.invoke( this, args );

            if ("app".equals( method.getName() ) && method.getDeclaringClass().equals( Config.class ))
                return getAppConfig();

            Config.Group configGroupAnnotation = findAnnotation( method.getReturnType(), Config.Group.class );
            if (configGroupAnnotation != null)
                // Method return type is annotated with @Group, it's a config group: return a proxy for it.
                return getDefaultImplementation( proxyPrefix, method.getReturnType() );

            // Method does not return a config group; get its value.
            return getPropertyValueFor( proxyPrefix, method );
        }

        @Override
        public String toString() {

            return String.format( "%s.%s#%x", DefaultConfigFactory.this, getClass().getSimpleName(), hashCode() );
        }
    }

    private class DefaultWrapperHandler implements InvocationHandler {

        private final Object config;

        public DefaultWrapperHandler(Object config) {

            this.config = config;
        }

        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {

            if (method.getDeclaringClass().equals(Object.class ))
                return method.invoke( this, args );

            Object value = method.invoke( config, args );

            if (value == null)
                value = getAnnotationValueFor( method );

            Config.Group configGroupAnnotation = findAnnotation( method.getReturnType(), Config.Group.class );
            if (configGroupAnnotation != null)
                // Method return type is annotated with @Group, it's a config group: return a wrapper for it.
                value = getDefaultWrapper( value );

            logger.debug( method.getReturnType().getSimpleName() + " " + method.getDeclaringClass().getSimpleName() + "#" + method.getName()
                          + " = " + value );
            return value;
        }

        @Override
        public String toString() {

            return String.format( "%s.%s#%x", DefaultConfigFactory.this, getClass().getSimpleName(), hashCode() );
        }
    }
}
