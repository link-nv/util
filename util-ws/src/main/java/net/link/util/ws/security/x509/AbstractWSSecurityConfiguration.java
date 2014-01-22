/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.ws.security.x509;

import org.joda.time.Duration;


/**
 * WS-Security configuration.
 *
 * @author lhunath
 */
public abstract class AbstractWSSecurityConfiguration implements WSSecurityConfiguration {

    public static final Duration DEFAULT_MAX_AGE = new Duration( 1000 * 60 * 5L );

    /**
     * @return the maximum age of the message.
     */
    @Override
    public Duration getMaximumAge() {

        return DEFAULT_MAX_AGE;
    }

    @Override
    public boolean isOutboundSignatureNeeded() {

        return true;
    }

    @Override
    public boolean isInboundSignatureOptional() {

        return true;
    }
}
