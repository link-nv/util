/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util.j2ee;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class TestClassLoader extends ClassLoader {

    private static final Log LOG = LogFactory.getLog( TestClassLoader.class );

    private final Map<String, List<URL>> resources;

    public TestClassLoader() {

        resources = new HashMap<String, List<URL>>();
    }

    @Override
    public Enumeration<URL> getResources(String name)
            throws IOException {

        LOG.debug( "get resources for resource name: " + name );
        List<URL> resourceList = resources.get( name );
        if (null == resourceList)
            return super.getResources( name );
        LOG.debug( "found test resources" );
        Enumeration<URL> enumeration = Collections.enumeration( resourceList );
        return enumeration;
    }

    @Override
    public InputStream getResourceAsStream(String name) {

        LOG.debug( "getResourceAsStream: " + name );
        List<URL> resourceList = resources.get( name );
        if (null == resourceList)
            return super.getResourceAsStream( name );
        for (URL resource : resourceList) {
            LOG.debug( "found resource: " + resource );
            try {
                InputStream inputStream = resource.openStream();
                return inputStream;
            } catch (IOException e) {
                LOG.debug( "error opening resource: " + resource );
            }
        }
        return null;
    }

    public void addResource(String name, URL resource) {

        if (null == resource)
            throw new IllegalArgumentException( "resource is null" );
        List<URL> resourceList = resources.get( name );
        if (null == resourceList) {
            resourceList = new LinkedList<URL>();
            resources.put( name, resourceList );
        }
        resourceList.add( resource );
    }
}
