/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.test.j2ee;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;


public class TestClassLoader extends ClassLoader {

    private final Map<String, List<URL>> resources;

    public TestClassLoader() {

        resources = new HashMap<String, List<URL>>();
    }

    @Override
    public Enumeration<URL> getResources(String name)
            throws IOException {

        List<URL> resourceList = resources.get( name );
        if (null == resourceList)
            return super.getResources( name );
        return Collections.enumeration( resourceList );
    }

    @Override
    public InputStream getResourceAsStream(String name) {

        List<URL> resourceList = resources.get( name );
        if (null == resourceList)
            return super.getResourceAsStream( name );
        for (URL resource : resourceList) {
            try {
                return resource.openStream();
            }
            catch (IOException e) {
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
