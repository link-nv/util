/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.wicket.component.input;

import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;


/**
 * <h2>{@link CustomRequiredTextArea}</h2>
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
public class CustomRequiredTextArea<T> extends TextArea<T> {

    private String requiredMessageKey = "errorFieldRequired";

    public CustomRequiredTextArea(String id) {

        super( id );
        setRequired( true );
    }

    public CustomRequiredTextArea(String id, final IModel<T> model) {

        super( id, model );
        setRequired( true );
    }

    public CustomRequiredTextArea<T> setRequiredMessageKey(String requiredMessageKey) {

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
