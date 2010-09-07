/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util.session;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.security.auth.spi.LoginModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


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

        if (false == LoginModule.class.isAssignableFrom( clazz ))
            throw new IllegalArgumentException( "given class is not a subclass of LoginModule" );
        File jaasConfigFile = File.createTempFile( "jaas-", ".login" );
        PrintWriter printWriter = new PrintWriter( jaasConfigFile );
        printWriter.println( "client-login {" );
        printWriter.println( clazz.getName() + " required debug=true;" );
        printWriter.println( "};" );
        printWriter.close();
        System.setProperty( "java.security.auth.login.config", jaasConfigFile.getAbsolutePath() );

        /*
         * We install a shutdown hook to cleanup the JAAS config file afterwards. Else we risk of flooding the /tmp directory with junk.
         */
        Runtime runtime = Runtime.getRuntime();
        JaasCleanupShutdownHook cleanupShutdownHook = new JaasCleanupShutdownHook( jaasConfigFile );
        runtime.addShutdownHook( cleanupShutdownHook );
    }

    private static class JaasCleanupShutdownHook extends Thread {

        private static final Log LOG = LogFactory.getLog( JaasCleanupShutdownHook.class );

        private final File jaasConfigFile;

        public JaasCleanupShutdownHook(File jaasConfigFile) {

            this.jaasConfigFile = jaasConfigFile;
        }

        @Override
        public void run() {

            LOG.debug( "cleanup JAAS config file: " + jaasConfigFile );
            if (false == jaasConfigFile.delete())
                jaasConfigFile.deleteOnExit();
        }
    }
}
