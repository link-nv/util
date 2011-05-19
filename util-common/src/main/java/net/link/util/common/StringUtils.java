/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.common;

/**
 * Sring utility class.
 *
 * @author wvdhaute
 */
public abstract class StringUtils {

    /**
     * Compares specified {@link String}'s in a constant time algorithm, preventing timing attacks
     *
     * @param str1 The string to compare from.
     * @param str2 The string to compare to.
     *
     * @return {@code true} if both strings contain the exact same characters.
     *
     * @see <a href="http://codahale.com/a-lesson-in-timing-attacks">
     */
    public static boolean isEqualConstant(String str1, String str2) {

        if (null == str1 && null == str2)
            return true;

        if (null == str2 || null == str1)
            return false;

        if (str1.length() != str2.length())
            return false;

        byte[] b1 = str1.getBytes();
        byte[] b2 = str2.getBytes();

        int result = 0;
        for (int i = 0; i < b1.length; i++)
            result |= b1[i] ^ b2[i];

        return result == 0;
    }
}
