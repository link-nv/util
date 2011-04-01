/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.wicket.component.input;

import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;


/**
 * <h2>{@link CustomRequiredPasswordTextField}</h2>
 * <p/>
 * <p>
 * [description / usage].
 * </p>
 * <p/>
 * <p>
 * <i>Mar 13, 2009</i>
 * </p>
 *
 * @author wvdhaute
 */
public class CustomRequiredPasswordTextField extends PasswordTextField {

    private String requiredMessageKey;

    public CustomRequiredPasswordTextField(String id) {

        super( id );
    }

    public CustomRequiredPasswordTextField(String id, final IModel<String> model) {

        super( id, model );
    }

    public void setRequiredMessageKey(String requiredMessageKey) {

        this.requiredMessageKey = requiredMessageKey;
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

        error( (IValidationError) new ValidationError().addMessageKey( requiredMessageKey ) );
    }
}
