package net.link.util.test.web;

import java.util.HashMap;
import java.util.Map;


/**
 * <h2>{@link AbstractSetup}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>10 01, 2010</i> </p>
 *
 * @author lhunath
 */
@SuppressWarnings( { "RawUseOfParameterizedType" })
public abstract class AbstractSetup<T> {

    private final Class<? extends T>  type;
    private final Map<String, String> initParameters;

    public AbstractSetup(Class<? extends T> type) {

        this.type = type;
        initParameters = new HashMap<String, String>();
    }

    public AbstractSetup addInitParameter(String key, String value) {

        initParameters.put( key, value );
        return this;
    }

    public Class<? extends T> getType() {

        return type;
    }

    public Map<String, String> getInitParameters() {

        return initParameters;
    }
}
