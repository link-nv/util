package net.link.util.logging;

import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;


/**
 * Credits go to: https://github.com/Lyndir/Opal
 */
public abstract class Markers {

    private static final BasicMarkerFactory factory = new BasicMarkerFactory();

    public static final Marker AUDIT    = factory.getMarker( "AUDIT" );
    public static final Marker BUG      = factory.getMarker( "BUG" );
    public static final Marker SECURITY = factory.getMarker( "SECURITY" );
}
