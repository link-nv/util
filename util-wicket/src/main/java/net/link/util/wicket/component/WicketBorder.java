package net.link.util.wicket.component;

import net.link.util.wicket.util.WicketUtils;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.model.*;


/**
 * Convenience wicket border subclass providing some generic functionality like i18n, ...
 */
public abstract class WicketBorder extends Border {

    protected WicketBorder(String id) {

        super( id );
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

                return WicketUtils.localize( WicketBorder.this, format, args );
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

        return new StringResourceModel( key, this, new Model<WicketBorder>( this ), args ).getString();
    }

    public AbstractReadOnlyModel<String> getLocalizedParameterModel(final String key, final String... args) {

        return new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {

                return getLocalizedParameterString( key, args );
            }
        };
    }
}
