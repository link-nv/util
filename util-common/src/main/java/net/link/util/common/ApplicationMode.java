package net.link.util.common;

import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;


public enum ApplicationMode {

    DEBUG,
    DEMO,
    DEPLOYMENT;

    public static final String PROPERTY = "applicationMode";

    public static ApplicationMode get(String mode) {

        for (ApplicationMode applicationMode : ApplicationMode.values()) {
            if (applicationMode.name().equals( mode ))
                return applicationMode;
        }

        throw new InternalInconsistencyException( String.format( "Invalid application mode \"%s\"", mode ) );
    }

    public static ApplicationMode get() {

        String modeString = System.getProperty( PROPERTY );
        if (null == modeString)
            return DEPLOYMENT;
        else
            return get( modeString );
    }
}
