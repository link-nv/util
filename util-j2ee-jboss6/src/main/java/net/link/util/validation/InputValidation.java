/*
 * SafeOnline project.
 *
 * Copyright 2005-2007 Frank Cornelis H.S.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import net.link.util.validation.annotation.ValidatorAnnotation;
import net.link.util.validation.validator.Validator;
import net.link.util.validation.validator.ValidatorCatalog;


/**
 * Basic input validation interceptor for EJB3.
 *
 * @author cornelis
 */
public class InputValidation {

    @AroundInvoke
    @SuppressWarnings("unchecked")
    public <A extends Annotation> Object inputValidationInterceptor(InvocationContext context)
            throws Exception {

        Method method = context.getMethod();
        InputValidationValidatorResult validatorResult = new InputValidationValidatorResult();
        Annotation[][] allParameterAnnotations = method.getParameterAnnotations();
        Object[] parameters = context.getParameters();
        for (int parameterIdx = 0; parameterIdx < allParameterAnnotations.length; parameterIdx++) {
            Annotation[] parameterAnnotations = allParameterAnnotations[parameterIdx];
            for (Annotation parameterAnnotation : parameterAnnotations) {
                ValidatorAnnotation validatorClassAnnotation = parameterAnnotation.annotationType().getAnnotation( ValidatorAnnotation.class );
                if (null == validatorClassAnnotation)
                    continue;
                Class<? extends Validator<A>> validatorClass = (Class<? extends Validator<A>>) validatorClassAnnotation.value();
                Validator<A> validator = ValidatorCatalog.getInstance( validatorClass );
                Object parameter = parameters[parameterIdx];
                validator.validate( parameter, parameterIdx, (A) parameterAnnotation, validatorResult );
            }
        }
        if (validatorResult.isEmpty())
            return context.proceed();
        throw new IllegalArgumentException( validatorResult.toString() );
    }
}
