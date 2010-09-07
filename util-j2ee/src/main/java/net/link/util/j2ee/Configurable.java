package net.link.util.j2ee;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface Configurable {

    public static final String defaultGroup = "overall";

    String group() default defaultGroup;

    String name() default "";

    InputType inputType() default InputType.FIELD;

    boolean optional() default false;

    public enum InputType {

        /**
         * A small field of input; can be a text field, a check box, or whatever is appropriate for the type.
         */
        FIELD,
        /**
         * A large field of text input.
         */
        AREA,
        /**
         * A drop-down allowing the choice between multiple options.
         */
        CHOICE;
    }
}
