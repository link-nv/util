/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.component;

import net.link.safeonline.wicket.behaviour.FocusOnReady;
import net.link.safeonline.wicket.util.WicketUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;


/**
 * <h2>{@link WicketPanel}</h2>
 *
 * <p>
 * [description / usage].
 * </p>
 *
 * <p>
 * <i>May 11, 2009</i>
 * </p>
 *
 * @author wvdhaute
 */
public abstract class WicketPanel extends Panel {

    private FocusOnReady focusOnReady;

    public WicketPanel(String id) {

        super( id );
    }

    /**
     * Note: You can use this method with a single argument, too. This will cause the first argument (format) to be evaluated as a
     * localization key.
     *
     * @param format The format specification for the arguments. See {@link String#format(java.util.Locale, String, Object...)}. To that
     * list,
     * add the 'l' conversion parameter. This parameter first looks the arg data up as a localization key, then processes the
     * result as though it was given with the 's' conversion parameter.
     * @param args The arguments that contain the data to fill into the format specifications.
     */
    public AbstractReadOnlyModel<String> localize(final String format, final Object... args) {

        return new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {

                return WicketUtils.localize( WicketPanel.this, format, args );
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

        return new StringResourceModel( key, this, new Model<WicketPanel>( this ), args ).getString();
    }

    public AbstractReadOnlyModel<String> getLocalizedParameterModel(final String key, final String... args) {

        return new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {

                return getLocalizedParameterString( key, args );
            }
        };
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
