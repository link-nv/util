/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.component.feedback;

import java.io.Serializable;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


/**
 * <h2>{@link ErrorComponentFeedbackLabel}<br>
 * <sub>Custom feedback label component only displaying error messages.</sub></h2>
 *
 * <p>
 * Custom feedback label component only displaying error messages. Introduced as the FeedbackPanel is a bit too heavy for most cases.
 * </p>
 *
 * <p>
 * <i>Nov 6, 2008</i>
 * </p>
 *
 * @author wvdhaute
 */
@SuppressWarnings("unchecked")
public class ErrorComponentFeedbackLabel extends Label {

    private static final Log LOG = LogFactory.getLog( ErrorComponentFeedbackLabel.class );

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
     * @param id The non-null id of this component
     * @param component The {@link FormComponent} to show the FeedbackMessage for.
     */
    public ErrorComponentFeedbackLabel(String id, Component component, IModel<?> text) {

        this( id, component );
        this.text = text;
    }

    /**
     * Call this constructor if you just want to display the FeedbackMessage of the component
     *
     * @param id The non-null id of this component
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

        Iterator<FeedbackMessage> messageIter = getSession().getFeedbackMessages().iterator();
        while (messageIter.hasNext()) {
            FeedbackMessage message = messageIter.next();
            LOG.debug( "message.message : " + message.getMessage() );
            LOG.debug( "message.reporter.id: " + message.getReporter().getId() );
            LOG.debug( "message.reporter.markupid: " + message.getReporter().getMarkupId() );
            LOG.debug( "component.id: " + component.getId() );
            LOG.debug( "component.markupId: " + component.getMarkupId() );
        }

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
