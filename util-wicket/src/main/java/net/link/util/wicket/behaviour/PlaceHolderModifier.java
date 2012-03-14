package net.link.util.wicket.behaviour;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;
import org.apache.wicket.model.IModel;


/**
 * Created by IntelliJ IDEA. User: sgdesmet Date: 31/10/11 Time: 14:47 To change this template use File | Settings | File Templates.
 */
public class PlaceHolderModifier extends AttributeModifier {

    Component component = null;

    boolean addFallbackJS;

    private static final ResourceReference PLACEHOLDER_JS = new JavascriptResourceReference(PlaceHolderModifier.class, "placeholder.js");

    /**
     * Adds the HTML5 placeholder tag, with an optional JS for browsers that don't support the tag
     *
     * @param model The text to display
     * @param addFallbackJS If true, ddd JS to the page for browsers that don't support the placeholder tag
     */
    public PlaceHolderModifier(IModel<String> model, boolean addFallbackJS){
        super("placeholder",true, model );
        this.addFallbackJS = addFallbackJS;
    }

    /**
     * Add a placeholder, and include JS on the page for browsers that don't support the placeholder tag
     * @param model
     */
    public PlaceHolderModifier(IModel<String> model){
        this(model, true);
    }

    /**
	 * Bind this handler to the given component.
	 *
	 * @param hostComponent
	 *            the component to bind to
	 */
	@Override
	public void bind(final Component hostComponent)
	{
        super.bind( hostComponent );
		if (hostComponent == null)
		{
			throw new IllegalArgumentException("Argument hostComponent must be not null");
		}

		if (component != null)
		{
			throw new IllegalStateException("this kind of handler cannot be attached to " +
				"multiple components; it is already attached to component " + component +
				", but component " + hostComponent + " wants to be attached too");
		}

		component = hostComponent;
	}


    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead( response );
        if (addFallbackJS)
            response.renderJavascriptReference( PLACEHOLDER_JS, "placeholder-script" );
    }


    public Component getComponent() {
        return component;
    }
}
