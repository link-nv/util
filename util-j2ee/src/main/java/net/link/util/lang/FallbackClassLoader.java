package net.link.util.lang;

import java.util.*;


/**
 * <h2>{@link FallbackClassLoader}<br> <sub>[in short] (TODO).</sub></h2>
 * <p/>
 * <p> <i>03 21, 2011</i> </p>
 *
 * @author lhunath
 */
public class FallbackClassLoader extends ClassLoader {

    private final List<ClassLoader> classLoaders = new LinkedList<ClassLoader>();

    public FallbackClassLoader(ClassLoader... classLoaders) {

        Collections.addAll( this.classLoaders, classLoaders );
    }

    @Override
    public Class<?> loadClass(final String name)
            throws ClassNotFoundException {

        for (ClassLoader classLoader : classLoaders)
            if (classLoader != null)
                try {
                    return classLoader.loadClass( name );
                }
                catch (ClassNotFoundException ignored) {
                }

        throw new ClassNotFoundException(
                "None of the supported ClassLoaders can load the class: " + name + "\nSupported ClassLoaders: " + classLoaders );
    }
}
