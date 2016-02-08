package net.link.util.util;

import net.link.util.InternalInconsistencyException;


/**
 * Created by wvdhaute
 * Date: 12/03/14
 * Time: 10:24
 */
public class AlreadyCheckedException extends InternalInconsistencyException {

    public AlreadyCheckedException(final String message, final Throwable cause) {

        super( message, cause );
    }
}
