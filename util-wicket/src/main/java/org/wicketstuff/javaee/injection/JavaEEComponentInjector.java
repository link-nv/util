/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wicketstuff.javaee.injection;

import net.link.util.j2ee.NamingStrategy;
import org.apache.wicket.injection.ComponentInjector;
import org.apache.wicket.injection.web.InjectorHolder;


/**
 * This injection must be initialized in the Wicket WebApplication in order to enable Java EE 5 resource injection in Wicket Pages Add the
 * initialization in WebApplication's init() method, e.g.
 * <p/>
 * protected void init() { addComponentInstantiationListener(new JavaEEComponentInjector(this)); }
 *
 * @author Filippo Diotalevi
 */
public class JavaEEComponentInjector extends ComponentInjector {

    /**
     * Constructor
     *
     * @param namingStrategy - a jndi naming strategy to lookup ejb references
     */
    public JavaEEComponentInjector(NamingStrategy namingStrategy) {

        InjectorHolder.setInjector( new AnnotJavaEEInjector( namingStrategy ) );
    }
}
