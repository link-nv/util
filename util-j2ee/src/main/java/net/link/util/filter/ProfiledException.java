/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.filter;

import java.util.Collections;
import java.util.Map;
import javax.servlet.ServletException;


/**
 * <h2>{@link ProfiledException} - A wrapper for exceptions that occurred during profiling.</h2>
 * <p>
 * This wrapper serves as an intermediate to make sure profiling data is still transferred to the agents when exceptions occur in linkID.<br>
 * <br>
 * Since HTTP headers are unavailable when exceptions occur, the profiling data is serialized as a {@link Map} of {@link String}s. The agent
 * unserializes this data to retrieve the profiling data.
 * </p>
 * <p>
 * <i>Nov 30, 2007</i>
 * </p>
 *
 * @author mbillemo
 */
public class ProfiledException extends ServletException {

    private Map<String, String> headers;

    /**
     * Create a new {@link ProfiledException} instance.
     */
    public ProfiledException(Throwable e, Map<String, String> headers) {

        super( e );

        this.headers = headers;
    }

    /**
     * @return The headers of this {@link ProfiledException}.
     */
    public Map<String, String> getHeaders() {

        return Collections.unmodifiableMap( headers );
    }
}
