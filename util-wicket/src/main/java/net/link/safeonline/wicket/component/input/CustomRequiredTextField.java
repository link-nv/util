/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.component.input;

import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;


/**
 * <h2>{@link CustomRequiredTextField}</h2>
 *
 * <p>
 * [description / usage].
 * </p>
 *
 * <p>
 * <i>Mar 13, 2009</i>
 * </p>
 *
 * @author wvdhaute
 */
public class CustomRequiredTextField<T> extends RequiredTextField<T> {

    private String requiredMessageKey = "errorFieldRequired";


    public CustomRequiredTextField(String id) {

        super( id );
    }

    public CustomRequiredTextField(String id, final IModel<T> model) {

        super( id, model );
    }

    public CustomRequiredTextField(String id, final IModel<T> model, final Class<T> type) {

        super( id, model, type );
    }

    public CustomRequiredTextField<T> setRequiredMessageKey(String requiredMessageKey) {

        this.requiredMessageKey = requiredMessageKey;

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate() {

        // validateRequired();
        if (!checkRequired())
            reportRequiredError();

        if (isValid()) {
            convertInput();

            if (isValid() && isRequired() && getConvertedInput() == null && isInputNullable())
                reportRequiredError();

            if (isValid())
                validateValidators();
        }
    }

    private void reportRequiredError() {

        if (null != requiredMessageKey)
            error( (IValidationError) new ValidationError().addMessageKey( requiredMessageKey ) );
        else
            error( (IValidationError) new ValidationError().addMessageKey( "Required" ) );
    }
}
