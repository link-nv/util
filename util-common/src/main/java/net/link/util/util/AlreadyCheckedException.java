package net.link.util.util;

import net.link.util.InternalInconsistencyException;


/**
 * Created by wvdhaute
 * Date: 12/03/14
 * Time: 10:24
 */
public class AlreadyCheckedException extends InternalInconsistencyException {

    public AlreadyCheckedException() {

        super( "BUG: A previous check designed to prevent this exception should exist but must have failed." );
    }

    public AlreadyCheckedException(final String message) {

        super( message );
    }

    public AlreadyCheckedException(final Throwable cause) {

        super( "BUG: A previous check designed to prevent this exception should exist but must have failed.", cause );
    }

    public AlreadyCheckedException(final String message, final Throwable cause) {

        super( message, cause );
    }
}
