/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.util.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.*;
import java.lang.reflect.Proxy;
import junit.framework.TestCase;
import net.sf.cglib.proxy.*;


public class EJBTestUtilsTest extends TestCase {

    public interface TestIface {

        void func();
    }


    public static class TestClass implements TestIface {

        public void func() {

        }
    }


    private static class TestInvocationHandler implements InvocationHandler {

        private final Object object;

        private TestInvocationHandler(Object object) {

            this.object = object;
        }

        @SuppressWarnings({ "ProhibitedExceptionDeclared" })
        public Object invoke(@SuppressWarnings("unused") Object proxy, Method method, Object[] args)
                throws Throwable {

            return method.invoke( object, args );
        }
    }

    public void testProxy()
            throws Exception {

        // Setup Data
        TestClass origObject = new TestClass();
        TestInvocationHandler testInvocationHandler = new TestInvocationHandler( origObject );
        TestIface testObject = (TestIface) Proxy.newProxyInstance( EJBTestUtilsTest.class.getClassLoader(), TestClass.class.getInterfaces(),
                testInvocationHandler );

        // Test
        testObject.func();

        // Verify
        assertFalse( testObject instanceof TestClass );
    }

    private static class TestInterceptor implements MethodInterceptor {

        private final Object object;

        private TestInterceptor(Object object) {

            this.object = object;
        }

        @SuppressWarnings({ "ProhibitedExceptionDeclared" })
        public Object intercept(@SuppressWarnings("unused") Object obj, Method method, Object[] args, @SuppressWarnings("unused") MethodProxy proxy)
                throws Throwable {

            return method.invoke( object, args );
        }
    }

    public void testCgLib()
            throws Exception {

        // Setup Data
        TestClass origObject = new TestClass();

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass( TestClass.class );
        TestInterceptor testInterceptor = new TestInterceptor( origObject );
        enhancer.setCallback( testInterceptor );
        TestClass object = (TestClass) enhancer.create();

        // Test
        object.func();

        // Verify
        // assertTrue(object instanceof TestClass);
        // ^ object is declared as TestClass, making this assert pretty useless
    }
}
