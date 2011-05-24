/*
 * Copyright (c) 2011 Encap A.S.
 ******************************************************************************/

package net.link.util.config;

import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import net.link.util.common.DummyServletRequest;
import org.jetbrains.annotations.Nullable;


public class TestConfigHolder<C extends RootConfig> extends ConfigHolder<C> {

    private static RootConfig testConfig;

    /**
     * Only use this method when you're certain a TestConfigHolder has been set as the active config.
     *
     * @param type The type of test config that was previously activated.
     *
     * @return The currently active config, cast to TestConfigHolder.
     */
    @SuppressWarnings( { "StaticVariableUsedBeforeInitialization" })
    public static <C extends RootConfig> C testConfig(Class<C> type) {

        return type.cast( testConfig );
    }

    public TestConfigHolder(C testConfig, @Nullable final ServletContext servletContext) {

        //noinspection unchecked
        super( new DefaultConfigFactory() {

                    @Nullable
                    @Override
                    @SuppressWarnings( { "RefusedBequest" })
                    protected ServletContext getServletContext() {

                        return servletContext;
                    }

                    @Override
                    @SuppressWarnings( { "RefusedBequest" })
                    protected ServletRequest getServletRequest() {

                        return new DummyServletRequest() {
                            @Override
                            @SuppressWarnings( { "RefusedBequest" })
                            public Locale getLocale() {

                                return new Locale( "en" );
                            }

                            @Override
                            @SuppressWarnings( { "RefusedBequest" })
                            public String getContextPath() {

                                return "/";
                            }
                        };
                    }
                }, (Class<C>)testConfig.getClass(), testConfig );

        TestConfigHolder.testConfig = testConfig;
    }

    public void install() {

        ConfigHolder.setGlobalConfigHolder( this );
    }
}
