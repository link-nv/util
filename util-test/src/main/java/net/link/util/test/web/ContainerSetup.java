package net.link.util.test.web;

import java.util.HashMap;
import java.util.Map;

/**
* <h2>{@link net.link.util.test.web.ContainerSetup}<br>
* <sub>[in short] (TODO).</sub></h2>
*
* <p>
* <i>10 01, 2010</i>
* </p>
*
* @author lhunath
*/
public class ContainerSetup {

    private final String        contextPath;
    private final ServletSetup servlet;
    private final FilterSetup[] filters;

    private final Map<String, String> contextParameters = new HashMap<String, String>();
    private final Map<String, Object> sessionAttributes = new HashMap<String, Object>();

    public ContainerSetup(ServletSetup servlet, FilterSetup... filters) {

        this( "/", servlet, filters );
    }

    public ContainerSetup(String contextPath, ServletSetup servlet, FilterSetup... filters) {

        this.contextPath = contextPath;
        this.servlet = servlet;
        this.filters = filters;
    }

    public ContainerSetup addContextParameter(String key, String value) {

        contextParameters.put( key, value );
        return this;
    }

    public ContainerSetup addSessionAttribute(String key, Object value) {

        sessionAttributes.put( key, value );
        return this;
    }

    public String getContextPath() {

        return contextPath;
    }

    public ServletSetup getServlet() {

        return servlet;
    }

    public FilterSetup[] getFilters() {

        return filters;
    }

    public Map<String, String> getContextParameters() {

        return contextParameters;
    }

    public Map<String, Object> getSessionAttributes() {

        return sessionAttributes;
    }
}
