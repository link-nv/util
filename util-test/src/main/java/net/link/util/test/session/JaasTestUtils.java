/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.test.session;

import java.io.*;
import javax.security.auth.spi.LoginModule;


/**
 * JAAS test utility class.
 *
 * @author fcorneli
 */
public class JaasTestUtils {

    private JaasTestUtils() {

        // empty
    }

    public static void initJaasLoginModule(Class<?> clazz)
            throws IOException {

        if (!LoginModule.class.isAssignableFrom( clazz ))
            throw new IllegalArgumentException( "given class is not a subclass of LoginModule" );
        File jaasConfigFile = File.createTempFile( "jaas-", ".login" );
        PrintWriter printWriter = new PrintWriter( jaasConfigFile );
        try {
            printWriter.println( "client-login {" );
            printWriter.println( clazz.getName() + " required debug=true;" );
            printWriter.println( "};" );
        }
        finally {
            printWriter.close();
        }
        System.setProperty( "java.security.auth.login.config", jaasConfigFile.getAbsolutePath() );

        /*
         * We install a shutdown hook to cleanup the JAAS config file afterwards. Else we risk of flooding the /tmp directory with junk.
         */
        Runtime.getRuntime().addShutdownHook( new Thread( new JaasCleanupShutdownHook( jaasConfigFile ) ) );
    }

    static class JaasCleanupShutdownHook implements Runnable {

        private final File jaasConfigFile;

        JaasCleanupShutdownHook(File jaasConfigFile) {

            this.jaasConfigFile = jaasConfigFile;
        }

        @Override
        public void run() {

            if (!jaasConfigFile.delete())
                jaasConfigFile.deleteOnExit();
        }
    }
}
