package net.link.util.common;

import com.google.common.base.Supplier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <h2>{@link ObjectUtils}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>10 20, 2010</i> </p>
 *
 * @author lhunath
 */
public abstract class ObjectUtils {

    static final Logger logger = LoggerFactory.getLogger( ObjectUtils.class );

    /**
     * @param value        The value to return, if it isn't <code>null</code> .
     * @param defaultValue The value to return if <code>value</code>  is <code>null</code> .
     * @param <T>          The type of object to return.
     *
     * @return One of two values.
     */
    public static <T> T getOrDefault(T value, T defaultValue) {

        if (value != null)
            return value;

        return defaultValue;
    }

    /**
     * Version of {@link #getOrDefault(Object, Object)} that loads the default value lazily.
     *
     * @param value                The value to return, if it isn't <code>null</code> .
     * @param defaultValueSupplier Provides the value to return if <code>value</code>  is <code>null</code>.  The supplier is only consulted
     *                             if necessary.
     * @param <T>                  The type of object to return.
     *
     * @return One of two values.
     */
    public static <T> T getOrDefault(T value, Supplier<T> defaultValueSupplier) {

        if (value != null)
            return value;

        return defaultValueSupplier.get();
    }

    /**
     * Recursively search a type's inheritance hierarchy for an annotation.
     *
     * @param type           The class whose hierarchy to search.
     * @param annotationType The annotation type to search for.
     * @param <A>            The annotation type.
     *
     * @return The annotation of the given annotation type in the given type's hierarchy or <code>null</code> if the type's hierarchy
     *         contains no classes that have the given annotation type set.
     */
    public static <A extends Annotation> A findAnnotation(Class<?> type, Class<A> annotationType) {

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

    /**
     * Recursively search a method's inheritance hierarchy for an annotation.
     *
     * @param method         The method whose hierarchy to search.
     * @param annotationType The annotation type to search for.
     * @param <A>            The annotation type.
     *
     * @return The annotation of the given annotation type in the given method's hierarchy or <code>null</code> if the method's hierarchy
     *         contains no methods that have the given annotation type set.
     */
    public static <A extends Annotation> A findAnnotation(Method method, Class<A> annotationType) {

        A annotation = method.getAnnotation( annotationType );
        if (annotation != null) {
            //logger.debug( "Found annotation {} on {}", annotationType, method );
            return annotation;
        }

        //logger.debug( "Digging down method for annotation {}, {}", annotationType, method );

        Method superclassMethod = null;
        Class<?> superclass = method.getDeclaringClass();
        while ((superclass = superclass.getSuperclass()) != null)
            try {
                //logger.debug( "Trying for method {} in {}", method.getName(), superclass );
                superclassMethod = superclass.getMethod( method.getName(), method.getParameterTypes() );
            } catch (NoSuchMethodException ignored) {
            }
        if (superclass == null) {
            //logger.debug( "Gave up for annotation {} (reached end of hierarchy)", annotationType );
            return null;
        }

        return findAnnotation( superclassMethod, annotationType );
    }

    /**
     * Recursively search a type's inheritance hierarchy for an annotation.
     *
     * @param type           The class whose hierarchy to search.
     * @param annotationType The annotation type to search for.
     *
     * @return true if the annotation exists in the type's hierarchy.
     */
    public static boolean hasAnnotation(Class<?> type, Class<? extends Annotation> annotationType) {

        return findAnnotation( type, annotationType ) != null;
    }

    @SuppressWarnings( { "unchecked" })
    public static <T> T unsafeEnumValueOf(Class<T> type, String value) {

        return type.cast( Enum.valueOf( (Class<Enum>) type, value ) );
    }
}
