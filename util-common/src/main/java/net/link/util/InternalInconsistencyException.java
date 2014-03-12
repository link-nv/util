package net.link.util;

/**
 * <h2>{@link InternalInconsistencyException}<br>
 * <sub>Indicate a bug.</sub></h2>
 * <p/>
 * <p>
 * When this exception is fired, something happened that under all expected circumstances should never happen. With other words, a BUG.
 * </p>
 * <p/>
 * <p>
 * <i>Feb 16, 2009</i>
 * </p>
 *
 * @author lhunath
 */
public class InternalInconsistencyException extends RuntimeException {

    private boolean handled;

    public InternalInconsistencyException(String bugDescription) {

        super( bugDescription );
    }

    public InternalInconsistencyException(String bugDescription, Throwable cause) {

        super( bugDescription, cause );
    }

    public InternalInconsistencyException(Throwable cause) {

        super( cause );
    }

    public void markHandled() {

        handled = true;
    }

    public boolean isHandled() {

        return handled;
    }

    @Override
    public String getMessage() {

        return String.format( "[handled: %s] %s", isHandled(), super.getMessage() );
    }
}
