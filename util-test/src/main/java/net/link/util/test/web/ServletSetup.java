package net.link.util.test.web;

import javax.servlet.Servlet;


/**
 * <h2>{@link ServletSetup}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>10 01, 2010</i> </p>
 *
 * @author lhunath
 */
public class ServletSetup extends AbstractSetup<Servlet> {

    public ServletSetup(Class<? extends Servlet> type) {

        super( type );
    }

    @Override
    public ServletSetup addInitParameter(String key, String value) {

        return (ServletSetup) super.addInitParameter( key, value );
    }
}
