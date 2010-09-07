/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.component.feedback;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;


/**
 * <h2>{@link ErrorComponentFeedbackPanel}<br>
 * <sub>Component Feedback panel only displaying error messages.</sub></h2>
 *
 * <p>
 * Component Feedback panel only displaying error messages.
 * </p>
 *
 * <p>
 * <i>Nov 6, 2008</i>
 * </p>
 *
 * @author wvdhaute
 */
public class ErrorComponentFeedbackPanel extends ComponentFeedbackPanel {

    public ErrorComponentFeedbackPanel(String id, Component filter) {

        super( id, filter );
    }

    @Override
    public boolean isVisible() {

        if (anyErrorMessage())
            return true;
        return false;
    }
}
