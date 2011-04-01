/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.wicket.component;

import net.link.util.wicket.behaviour.FocusOnReady;
import net.link.util.wicket.util.WicketUtils;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.*;


/**
 * <h2>{@link WicketPage}<br>
 * <sub>An abstract {@link WebPage} that provides some convenience methods for wicket work.</sub></h2>
 * <p/>
 * <p>
 * The {@link #localize(String, Object...)} method provides easy access to generating localized messages in wicket pages.
 * </p>
 * <p/>
 * <p>
 * <i>Dec 15, 2008</i>
 * </p>
 *
 * @author lhunath
 */
public abstract class WicketPage extends WebPage {

    private FocusOnReady focusOnReady;

    public WicketPage() {

        super();
    }

    public WicketPage(PageParameters parameters) {

        super( parameters );
    }

    /**
     * Note: You can use this method with a single argument, too. This will cause the first argument (format) to be evaluated as a
     * localization key.
     *
     * @param format The format specification for the arguments. See {@link String#format(java.util.Locale, String, Object...)}. To that
     *               list,
     *               add the 'l' conversion parameter. This parameter first looks the arg data up as a localization key, then processes the
     *               result as though it was given with the 's' conversion parameter.
     * @param args   The arguments that contain the data to fill into the format specifications.
     */
    public AbstractReadOnlyModel<String> localize(final String format, final Object... args) {

        return new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {

                return WicketUtils.localize( WicketPage.this, format, args );
            }
        };
    }

    public String getLocalizedString(String format, Object... args) {

        return WicketUtils.localize( this, format, args );
    }

    /**
     * This method uses the Wicket {@link StringResourceModel}
     */
    public String getLocalizedParameterString(String key, String... args) {

        return new StringResourceModel( key, this, new Model<WicketPage>( this ), args ).getString();
    }

    /**
     * Cause a component to be focussed referenced by the path given by element ids separated by colons.
     */
    public FocusOnReady focus(final Component component) {

        if (component == null)
            return null;

        if (focusOnReady == null)
            add( focusOnReady = new FocusOnReady( component ) );
        else
            focusOnReady.setComponent( component );

        return focusOnReady;
    }
}
