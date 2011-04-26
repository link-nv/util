/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.jpa;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import net.link.util.jpa.annotation.*;


/**
 * Invocation handler for the query object factory. The query object factory is using the Proxy API to construct the query object. The
 * behaviour of the query object is provided via this invocation handler.
 *
 * @author fcorneli
 */
public class QueryObjectInvocationHandler implements InvocationHandler {

    private final EntityManager entityManager;

    public QueryObjectInvocationHandler(EntityManager entityManager) {

        this.entityManager = entityManager;
    }

    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {

        QueryMethod queryMethodAnnotation = method.getAnnotation( QueryMethod.class );
        if (null != queryMethodAnnotation)
            return query( queryMethodAnnotation, method, args );

        UpdateMethod updateMethodAnnotation = method.getAnnotation( UpdateMethod.class );
        if (null != updateMethodAnnotation)
            return update( updateMethodAnnotation, method, args );

        throw new RuntimeException( "@QueryMethod or @UpdateMethod annotation expected: " + method.getDeclaringClass().getName() );
    }

    private Object update(UpdateMethod updateMethodAnnotation, Method method, Object[] args) {

        String namedQueryName = updateMethodAnnotation.value();
        Query query = entityManager.createNamedQuery( namedQueryName );
        setParameters( method, args, query );

        Class<?> returnType = method.getReturnType();

        if (Query.class.isAssignableFrom( returnType ))
            return query;

        Integer result = query.executeUpdate();

        if (Integer.TYPE.isAssignableFrom( returnType ))
            return result;
        return null;
    }

    private Object query(QueryMethod queryMethodAnnotation, Method method, Object[] args) {

        String namedQueryName = queryMethodAnnotation.value();
        if (namedQueryName == null || namedQueryName.isEmpty()) {
            StringBuilder name = new StringBuilder( method.getName() );
            Class<?> type = method.getDeclaringClass();
            do {
                name.insert( 0, '.' ).insert( 0, type.getSimpleName() );
                type = type.getEnclosingClass();
            }
            while (type != null);
            namedQueryName = name.toString();
        }
        Query query = entityManager.createNamedQuery( namedQueryName );

        setParameters( method, args, query );

        Class<?> returnType = method.getReturnType();
        if (Query.class.isAssignableFrom( returnType ))
            return query;
        if (List.class.isAssignableFrom( returnType ))
            return query.getResultList();

        if (queryMethodAnnotation.nullable()) {
            List<?> resultList = query.getResultList();
            if (resultList.isEmpty())
                return null;

            return resultList.get( 0 );
        }

        return query.getSingleResult();
    }

    private void setParameters(Method method, Object[] args, Query query) {

        if (null == args)
            return;

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int paramIdx = 0; paramIdx < args.length; paramIdx++)
            for (Annotation parameterAnnotation : parameterAnnotations[paramIdx])
                if (parameterAnnotation instanceof QueryParam) {
                    QueryParam queryParamAnnotation = (QueryParam) parameterAnnotation;
                    String paramName = queryParamAnnotation.value();
                    if (paramName == null || paramName.isEmpty())
                        query.setParameter( paramIdx, args[paramIdx] );
                    else
                        query.setParameter( paramName, args[paramIdx] );
                }
    }
}
