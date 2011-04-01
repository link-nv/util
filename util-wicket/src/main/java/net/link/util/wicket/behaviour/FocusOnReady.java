/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.wicket.behaviour;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractHeaderContributor;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;


/**
 * <h2>{@link FocusOnReady}</h2>
 * <p/>
 * <p>
 * [description / usage].
 * </p>
 * <p/>
 * <p>
 * <i>Jan 21, 2009</i>
 * </p>
 *
 * @author lhunath
 */
public class FocusOnReady extends AbstractHeaderContributor {

    private IHeaderContributor headercontributer;

    public FocusOnReady(Component component) {

        component.setOutputMarkupId( true );
        setComponent( component );
    }

    public void setComponent(final Component component) {

        headercontributer = new IHeaderContributor() {

            public void renderHead(IHeaderResponse response) {

                if (component.isVisibleInHierarchy()) {
                    String id = component.getMarkupId( true );
                    response.renderOnDomReadyJavascript(
                            String.format( "document.getElementById('%s').focus()", id.replaceAll( "'", "\\'" ) ) );
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IHeaderContributor[] getHeaderContributors() {

        return new IHeaderContributor[] { headercontributer };
    }
}
