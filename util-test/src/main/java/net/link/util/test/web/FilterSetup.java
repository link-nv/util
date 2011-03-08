package net.link.util.test.web;

import javax.servlet.Filter;


/**
 * <h2>{@link FilterSetup}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>10 01, 2010</i> </p>
 *
 * @author lhunath
 */
public class FilterSetup extends AbstractSetup<Filter> {

    public FilterSetup(Class<? extends Filter> type) {

        super( type );
    }

    @Override
    public FilterSetup addInitParameter(String key, String value) {

        return (FilterSetup) super.addInitParameter( key, value );
    }
}
