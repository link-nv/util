/*
 * SafeOnline project.
 *
 * Copyright 2005-2007 Frank Cornelis H.S.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.validation.validator;

import java.lang.annotation.Annotation;


/**
 * Interface for basic validators. Every validator has a corresponding annotation.
 *
 * @author fcorneli
 * @param <T>
 */
public interface Validator<T extends Annotation> {

    void validate(Object value, int parameterIdx, T parameterAnnotation, ValidatorResult result);
}
