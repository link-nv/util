/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.wicket.component.feedback;

import java.io.Serializable;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


/**
 * <h2>{@link ErrorComponentFeedbackLabel}<br> <sub>Custom feedback label component only displaying error messages.</sub></h2>
 * <p/>
 * <p> Custom feedback label component only displaying error messages. Introduced as the FeedbackPanel is a bit too heavy for most cases.
 * </p>
 * <p/>
 * <p> <i>Nov 6, 2008</i> </p>
 *
 * @author wvdhaute
 */
@SuppressWarnings("unchecked")
public class ErrorComponentFeedbackLabel extends Label {

    /**
     * Field component holds a reference to the {@link Component} this FeedbackLabel belongs to
     */
    private Component component;
    /**
     * Field text holds a model of the text to be shown in the FeedbackLabel
     */
    private IModel<?> text = null;

    /**
     * Call this constructor if you just want to display the FeedbackMessage of the component
     *
     * @param id        The non-null id of this component
     * @param component The {@link FormComponent} to show the FeedbackMessage for.
     */
    public ErrorComponentFeedbackLabel(String id, Component component, IModel<?> text) {

        this( id, component );
        this.text = text;
    }

    /**
     * Call this constructor if you just want to display the FeedbackMessage of the component
     *
     * @param id        The non-null id of this component
     * @param component The {@link FormComponent} to show the FeedbackMessage for.
     */
    public ErrorComponentFeedbackLabel(String id, Component component) {

        super( id );
        this.component = component;
    }

    /**
     * Set the content of this FeedbackLabel, depending on if the component has a FeedbackMessage.
     *
     * @see Component
     */
    @Override
    protected void onBeforeRender() {

        setDefaultModel( null );

        if (component.getFeedbackMessage() != null) {
            if (text != null)
                setDefaultModel( text );
            else
                setDefaultModel( new Model<Serializable>( component.getFeedbackMessage().getMessage() ) );
            component.getFeedbackMessage().markRendered();
        } else
            setDefaultModel( null );

        super.onBeforeRender();
    }

    @Override
    public boolean isVisible() {

        if (component.getFeedbackMessage() != null && component.getFeedbackMessage().getLevel() == FeedbackMessage.ERROR)
            return true;
        return false;
    }
}
