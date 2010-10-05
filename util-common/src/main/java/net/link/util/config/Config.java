package net.link.util.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <h2>{@link Config}<br>
 * <sub>[in short] (TODO).</sub></h2>
 *
 * <p>
 * <i>09 14, 2010</i>
 * </p>
 *
 * @author lhunath
 */
@Config.Group(prefix = "")
public interface Config {

    /**
     * @param appConfigType Your application's configuration class.
     *
     * @return Application-specific configuration.
     */

    <C extends AppConfig> C
    app(Class<C> appConfigType);

    /**
     * <h2>{@link Property}<br>
     * <sub>[in short] (TODO).</sub></h2>
     *
     * <p>
     * <i>09 15, 2010</i>
     * </p>
     *
     * @author lhunath
     */
    @Retention(RetentionPolicy.RUNTIME)
            @interface Group {

        String prefix();
    }

    /**
     * <h2>{@link Property}<br>
     * <sub>[in short] (TODO).</sub></h2>
     *
     * <p>
     * <i>09 15, 2010</i>
     * </p>
     *
     * @author lhunath
     */
    @Retention(RetentionPolicy.RUNTIME)
            @interface Property {

        String NONE = "Config.Property.NONE";
        String AUTO = "Config.Property.AUTO";

        boolean required();

        String unset() default NONE;
    }
}
