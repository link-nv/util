package net.link.util.util;

import java.lang.annotation.*;


/**
 * Credits go to: https://github.com/Lyndir/Opal
 */
@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectMeta {

    /**
     * @return The name to use for this field.  If unset (empty or null), the field's name will be used.
     */
    String name() default "";

    /**
     * @return What utilities can use this.
     */
    For[] useFor() default For.all;

    /**
     * @return What utilities may not use this, regardless of what other applicable {@link ObjectMeta} configuration says.
     */
    For[] ignoreFor() default { };

    /**
     * @return Allow this annotation to apply to subtypes of the type upon which it is declared (or the type which implements this type, if
     * this type is an interface).
     */
    boolean inherited() default true;

    enum For {

        /**
         * Add this field to toString generation of {@link ObjectUtils#toString()}.
         */
        toString,

        /**
         * Add this field to hashCode generation of {@link ObjectUtils#hashCode()}.
         */
        hashCode,

        /**
         * Add this field to equals generation of {@link ObjectUtils#equals(Object, Object)}.
         */
        equals,

        /**
         * Add this field to equals generation of {@link ObjectUtils#equals(Object, Object)}.
         */
        all
    }
}
