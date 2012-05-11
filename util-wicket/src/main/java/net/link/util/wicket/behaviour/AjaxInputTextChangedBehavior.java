package net.link.util.wicket.behaviour;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;


public abstract class AjaxInputTextChangedBehavior extends AbstractDefaultAjaxBehavior {

    private static final ResourceReference TEXTCHANGED_JS = new JavascriptResourceReference( AjaxInputTextChangedBehavior.class,
            "textchanged.js" );

    private static final long serialVersionUID = 1L;

    protected boolean enableClientTimer;

    /**
     * Creates an ajax event behavior which triggers when the content of the input text field has changed.
     * It does so by appending a client side javascript which binds to 'oninput', 'onpropertychange', 'oncut', 'onpaste' and 'onkeyup' and
     * for each of these events checks if the content of the field has changed. If so, a callback to wicket is made.
     * <p/>
     * It is also possible to enable a client side timer to check every 100ms to see if the content of the field has changed. This allows
     * to check if the content of the field has been changed by javascript.
     */
    protected AjaxInputTextChangedBehavior(boolean enableClientTimer) {

        this.enableClientTimer = enableClientTimer;
    }

    public boolean isEnableClientTimer() {

        return enableClientTimer;
    }

    public void setEnableClientTimer(final boolean enableClientTimer) {

        this.enableClientTimer = enableClientTimer;
    }

    @Override
    public void renderHead(IHeaderResponse response) {

        super.renderHead( response );
        renderTextchangedHead( response );
    }

    /**
     * Render init and javascript
     */
    private void renderTextchangedHead(IHeaderResponse response) {

        response.renderJavascriptReference( TEXTCHANGED_JS );
        final String id = getComponent().getMarkupId();

        String initJS = String.format( String.format( "new Wicket.TextChanged('%%s','%%s',%s);", enableClientTimer ), id,
                getCallbackUrl() );
        response.renderOnLoadJavascript( initJS );
    }

    @Override
    protected void onBind() {
        // add empty AbstractDefaultAjaxBehavior to the component, to force
        // rendering wicket-ajax.js reference if no other ajax behavior is on
        // page
        getComponent().add( new AbstractDefaultAjaxBehavior() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void respond(AjaxRequestTarget target) {

            }
        } );
    }

    /**
     * Called when the value of the text field changes. Also provides the current value of the field.
     */
    protected abstract void onInputChanged(String currentValue, AjaxRequestTarget target);

    @Override
    protected void respond(AjaxRequestTarget target) {

        final RequestCycle requestCycle = RequestCycle.get();
        final String val = requestCycle.getRequest().getParameter( "tval" );
        onInputChanged( val, target );
    }
}
