/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.filter;

import java.util.*;


/**
 * Generic filter util class. Use this to filter on all kind of collections. The filter logic itself should be implemented using the
 * {@link Filter} interface.
 *
 * @author fcorneli
 */
public class FilterUtil {

    public static <Type> List<Type> filter(List<Type> inputList, Filter<Type> filter) {

        List<Type> outputList = new LinkedList<Type>();
        for (Type element : inputList) {
            if (false == filter.isAllowed( element ))
                continue;
            outputList.add( element );
        }
        return outputList;
    }

    public static <Type> Set<Type> filter(Set<Type> inputSet, Filter<Type> filter) {

        Set<Type> outputSet = new HashSet<Type>();
        for (Type element : inputSet) {
            if (false == filter.isAllowed( element ))
                continue;
            outputSet.add( element );
        }
        return outputSet;
    }

    public static <KeyType, ValueType> Map<KeyType, ValueType> filter(Map<KeyType, ValueType> inputMap,
                                                                      MapEntryFilter<KeyType, ValueType> filter) {

        Map<KeyType, ValueType> outputMap = new HashMap<KeyType, ValueType>();
        for (Map.Entry<KeyType, ValueType> entry : inputMap.entrySet()) {
            if (false == filter.isAllowed( entry ))
                continue;
            outputMap.put( entry.getKey(), entry.getValue() );
        }
        return outputMap;
    }

    public static <KeyType, ValueType> Set<KeyType> filterToSet(Map<KeyType, ValueType> inputMap,
                                                                MapEntryFilter<KeyType, ValueType> filter) {

        Set<KeyType> outputSet = new HashSet<KeyType>();
        for (Map.Entry<KeyType, ValueType> entry : inputMap.entrySet()) {
            if (false == filter.isAllowed( entry ))
                continue;
            outputSet.add( entry.getKey() );
        }
        return outputSet;
    }

    public static <KeyType, ValueType> List<KeyType> filterToList(Map<KeyType, ValueType> inputMap,
                                                                  MapEntryFilter<KeyType, ValueType> filter) {

        List<KeyType> outputList = new LinkedList<KeyType>();
        for (Map.Entry<KeyType, ValueType> entry : inputMap.entrySet()) {
            if (false == filter.isAllowed( entry ))
                continue;
            outputList.add( entry.getKey() );
        }
        return outputList;
    }
}
