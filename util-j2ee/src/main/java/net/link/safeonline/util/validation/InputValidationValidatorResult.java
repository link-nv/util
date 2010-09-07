/*
 * SafeOnline project.
 *
 * Copyright 2005-2007 Frank Cornelis H.S.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.validation;

import java.util.LinkedList;
import java.util.List;
import net.link.safeonline.util.validation.validator.ValidatorResult;


public class InputValidationValidatorResult implements ValidatorResult {

    private List<String> results;

    InputValidationValidatorResult() {

        results = new LinkedList<String>();
    }

    public void addResult(String result) {

        results.add( result );
    }

    boolean isEmpty() {

        return results.isEmpty();
    }

    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer();
        for (String result : results)
            buffer.append( result );
        return buffer.toString();
    }
}
