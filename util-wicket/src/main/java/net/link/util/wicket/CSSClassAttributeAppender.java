package net.link.util.wicket;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import java.util.*;
import javax.annotation.Nullable;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;


/**
 * Created by wvdhaute
 * Date: 12/03/14
 * Time: 11:42
 */
public class CSSClassAttributeAppender extends AttributeAppender {

    private static final String CLASS_ATTRIBUTE = "class";
    private static final String CLASS_SEPARATOR = " ";

    /**
     * @param cssClassesModel A model that provides CSS classes to append to the element's <code>class</class> attribute.
     *
     * @return An appender which appends all the CSS classes in the collection from the given model to a component's HTML element.
     */
    public static CSSClassAttributeAppender ofList(final IModel<? extends Collection<String>> cssClassesModel) {

        // noinspection RedundantCast
        return new CSSClassAttributeAppender( cssClassesModel, (Collection<?>) null );
    }

    /**
     * @param cssClassModels Models that provides CSS classes to append to the element's <code>class</class> attribute.
     *
     * @return An appender which appends all the CSS classes in the collection from the given model to a component's HTML element.
     */
    public static CSSClassAttributeAppender of(final IModel<?>... cssClassModels) {

        return ofList( new AbstractReadOnlyModel<Collection<String>>() {

            @Override
            public Collection<String> getObject() {

                return Collections2.transform( Arrays.asList( cssClassModels ), new Function<IModel<?>, String>() {

                    @Nullable
                    @Override
                    public String apply(final IModel<?> from) {

                        return from.getObject() == null? null: String.valueOf( from.getObject() );
                    }
                } );
            }
        } );
    }

    /**
     * @param cssClassModels Models that provides CSS classes to append to the element's <code>class</class> attribute.
     *
     * @return An appender which appends all the CSS classes in the collection from the given model to a component's HTML element.
     */
    public static CSSClassAttributeAppender of(final String... cssClassModels) {

        return new CSSClassAttributeAppender( cssClassModels );
    }

    /**
     * @param cssClassModel A model that provides a CSS class to append to the element's <code>class</class> attribute.
     *
     * @return An appender which appends the CSS class in the model to a component's HTML element.
     */
    public static CSSClassAttributeAppender ofString(final IModel<String> cssClassModel) {

        // noinspection RedundantCast
        return new CSSClassAttributeAppender( cssClassModel, (String) null );
    }

    /**
     * @param cssClass The CSS class to append to the element's <code>class</class> attribute.
     */
    public CSSClassAttributeAppender(final String cssClass) {

        // noinspection RedundantCast
        this( new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {

                return cssClass;
            }
        }, (String) null );
    }

    /**
     * @param cssClasses An array of CSS classes to append to the element's <code>class</class> attribute.
     */
    public CSSClassAttributeAppender(final String... cssClasses) {

        // noinspection RedundantCast
        this( new AbstractReadOnlyModel<List<String>>() {

            @Override
            public List<String> getObject() {

                return Arrays.asList( cssClasses );
            }
        }, (Collection<?>) null );
    }

    private CSSClassAttributeAppender(final IModel<? extends Collection<String>> appendModel, @Nullable @SuppressWarnings("unused") final Collection<?> x) {

        // noinspection RedundantCast
        this( new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {

                StringBuilder stringBuilder = new StringBuilder();

                for (final String item : appendModel.getObject())
                    if (item != null && !item.isEmpty())
                        stringBuilder.append( item ).append( CLASS_SEPARATOR );

                if (stringBuilder.length() > 0)
                    stringBuilder.deleteCharAt( stringBuilder.length() - 1 );

                return stringBuilder.toString();
            }
        }, (String) null );
    }

    private CSSClassAttributeAppender(final IModel<String> appendModel, @Nullable @SuppressWarnings("unused") final String x) {

        super( CLASS_ATTRIBUTE, true, appendModel, CLASS_SEPARATOR );
    }
}
