/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.component.input;

import java.util.List;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;


/**
 * <h2>{@link RequiredDropDownChoice}</h2>
 * 
 * <p>
 * Required drop down choice with easier configurable required message.
 * </p>
 * 
 * <p>
 * <i>Aug 12, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class RequiredDropDownChoice<T> extends DropDownChoice<T> {

    private String requiredMessageKey = "errorFieldRequired";


    public RequiredDropDownChoice(String id) {

        super( id );
    }

    /**
     * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, List)
     */
    public RequiredDropDownChoice(final String id, final List<? extends T> choices) {

        super( id, choices );
    }

    /**
     * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, List,IChoiceRenderer)
     */
    public RequiredDropDownChoice(final String id, final List<? extends T> data, final IChoiceRenderer<T> renderer) {

        super( id, data, renderer );
    }

    /**
     * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel, List)
     */
    public RequiredDropDownChoice(final String id, IModel<T> model, final List<? extends T> choices) {

        super( id, model, choices );
    }

    /**
     * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel, List, IChoiceRenderer)
     */
    public RequiredDropDownChoice(final String id, IModel<T> model, final List<? extends T> data, final IChoiceRenderer<T> renderer) {

        super( id, model, data, renderer );
    }

    /**
     * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel)
     */
    public RequiredDropDownChoice(String id, IModel<List<? extends T>> choices) {

        super( id, choices );
    }

    /**
     * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,IModel)
     */
    public RequiredDropDownChoice(String id, IModel<T> model, IModel<List<? extends T>> choices) {

        super( id, model, choices );
    }

    /**
     * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel,IChoiceRenderer)
     */
    public RequiredDropDownChoice(String id, IModel<List<? extends T>> choices, IChoiceRenderer<T> renderer) {

        super( id, choices, renderer );
    }

    /**
     * @see org.apache.wicket.markup.html.form.AbstractChoice#AbstractChoice(String, IModel, IModel,IChoiceRenderer)
     */
    public RequiredDropDownChoice(String id, IModel<T> model, IModel<List<? extends T>> choices, IChoiceRenderer<T> renderer) {

        super( id, model, choices, renderer );
    }

    public void setRequiredMessageKey(String requiredMessageKey) {

        this.requiredMessageKey = requiredMessageKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRequired() {

        return true;
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
