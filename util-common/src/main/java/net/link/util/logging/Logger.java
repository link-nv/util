package net.link.util.logging;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Throwables;
import java.io.Serializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;


/**
 * Credits go to: https://github.com/Lyndir/Opal
 */
public class Logger implements Serializable {

    private final     String           name;
    private transient org.slf4j.Logger logger;

    // Create a logger --

    /**
     * @param type The type of the class that is responsible for the events logged through the logger that is returned.
     *
     * @return A logger.
     */
    public static Logger get(final Class<?> type) {

        return new Logger( type.getCanonicalName() );
    }

    public static Logger get(final String name) {

        return new Logger( name );
    }

    // Event logging --

    /**
     * Log a progress trace event.
     * <p/>
     * <p> This level is for all events that describe the flow of execution. </p>
     *
     * @param marker               An optional marker that can be used to tag this event with additional context.
     * @param cause                A throwable that details the stack at the time of this event.
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return Self, for chaining.
     */
    public Logger trc(@Nullable final Marker marker, @Nullable final Throwable cause, final String descriptionFormat, final Object... descriptionArguments) {

        if (slf4j().isTraceEnabled()) {
            if (null == descriptionArguments || 0 == descriptionArguments.length) {
                slf4j().trace( marker, descriptionFormat, cause );
            } else {
                slf4j().trace( marker, String.format( descriptionFormat, descriptionArguments ), cause );
            }
        }

        return this;
    }

    /**
     * Log a progress trace event.
     * <p/>
     * <p> This level is for all events that describe the flow of execution. </p>
     *
     * @param cause                A throwable that details the stack at the time of this event.
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return Self, for chaining.
     */
    public Logger trc(@Nullable final Throwable cause, final String descriptionFormat, final Object... descriptionArguments) {

        return trc( null, cause, descriptionFormat, descriptionArguments );
    }

    /**
     * Log a progress trace event.
     * <p/>
     * <p> This level is for all events that describe the flow of execution. </p>
     *
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return Self, for chaining.
     *
     * @see #trc(Throwable, String, Object...)
     */
    public Logger trc(final String descriptionFormat, final Object... descriptionArguments) {

        return trc( null, descriptionFormat, descriptionArguments );
    }

    /**
     * Log a debugging event.
     * <p/>
     * <p> This level is for all events that visualize the application's state. </p>
     *
     * @param marker               An optional marker that can be used to tag this event with additional context.
     * @param cause                A throwable that details the stack at the time of this event.
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return Self, for chaining.
     */
    public Logger dbg(@Nullable final Marker marker, @Nullable final Throwable cause, final String descriptionFormat, final Object... descriptionArguments) {

        if (slf4j().isDebugEnabled()) {
            if (null == descriptionArguments || 0 == descriptionArguments.length) {
                slf4j().debug( marker, descriptionFormat, cause );
            } else {
                slf4j().debug( marker, String.format( descriptionFormat, descriptionArguments ), cause );
            }
        }

        return this;
    }

    /**
     * Log a debugging event.
     * <p/>
     * <p> This level is for all events that visualize the application's state. </p>
     *
     * @param cause                A throwable that details the stack at the time of this event.
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return Self, for chaining.
     */
    public Logger dbg(@Nullable final Throwable cause, final String descriptionFormat, final Object... descriptionArguments) {

        return dbg( null, cause, descriptionFormat, descriptionArguments );
    }

    /**
     * Log a debugging event.
     * <p/>
     * <p> This level is for all events that visualize the application's state. </p>
     *
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return Self, for chaining.
     */
    public Logger dbg(final String descriptionFormat, final Object... descriptionArguments) {

        return dbg( null, descriptionFormat, descriptionArguments );
    }

    /**
     * Log an informative statement.
     * <p/>
     * <p> This level is for all events that detail an important evolution in the application's state. </p>
     *
     * @param marker               An optional marker that can be used to tag this event with additional context.
     * @param cause                A throwable that details the stack at the time of this event.
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return Self, for chaining.
     */
    public Logger inf(@Nullable final Marker marker, @Nullable final Throwable cause, final String descriptionFormat, final Object... descriptionArguments) {

        if (slf4j().isInfoEnabled()) {
            if (null == descriptionArguments || 0 == descriptionArguments.length) {
                slf4j().info( marker, descriptionFormat, cause );
            } else {
                slf4j().info( marker, String.format( descriptionFormat, descriptionArguments ), cause );
            }
        }

        return this;
    }

    /**
     * Log an informative statement.
     * <p/>
     * <p> This level is for all events that detail an important evolution in the application's state. </p>
     *
     * @param cause                A throwable that details the stack at the time of this event.
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return Self, for chaining.
     */
    public Logger inf(@Nullable final Throwable cause, final String descriptionFormat, final Object... descriptionArguments) {

        return inf( null, cause, descriptionFormat, descriptionArguments );
    }

    /**
     * Log an informative statement.
     * <p/>
     * <p> This level is for all events that detail an important evolution in the application's state. </p>
     *
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return Self, for chaining.
     *
     * @see #inf(Throwable, String, Object...)
     */
    public Logger inf(final String descriptionFormat, final Object... descriptionArguments) {

        return inf( null, descriptionFormat, descriptionArguments );
    }

    /**
     * Log an application warning.
     * <p/>
     * <p> This level is for all events that indicate a suboptimal / non-ideal flow. </p>
     *
     * @param marker               An optional marker that can be used to tag this event with additional context.
     * @param cause                A throwable that details the stack at the time of this event.
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return Self, for chaining.
     */
    public Logger wrn(@Nullable final Marker marker, @Nullable final Throwable cause, final String descriptionFormat, final Object... descriptionArguments) {

        if (slf4j().isWarnEnabled()) {
            if (null == descriptionArguments || 0 == descriptionArguments.length) {
                slf4j().warn( marker, descriptionFormat, cause );
            } else {
                slf4j().warn( marker, String.format( descriptionFormat, descriptionArguments ), cause );
            }
        }

        return this;
    }

    /**
     * Log an application warning.
     * <p/>
     * <p> This level is for all events that indicate a suboptimal / non-ideal flow. </p>
     *
     * @param cause                A throwable that details the stack at the time of this event.
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return Self, for chaining.
     */
    public Logger wrn(@Nullable final Throwable cause, final String descriptionFormat, final Object... descriptionArguments) {

        return wrn( null, cause, descriptionFormat, descriptionArguments );
    }

    /**
     * Log an application warning.
     * <p/>
     * <p> This level is for all events that indicate a suboptimal / non-ideal flow. </p>
     *
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return Self, for chaining.
     */
    public Logger wrn(final String descriptionFormat, final Object... descriptionArguments) {

        return wrn( null, descriptionFormat, descriptionArguments );
    }

    /**
     * Log an application error.
     * <p/>
     * <p> This level is for all events that indicate failure to comply with the request. </p>
     *
     * @param marker               An optional marker that can be used to tag this event with additional context.
     * @param cause                A throwable that details the stack at the time of this event.
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return Self, for chaining.
     */
    public Logger err(@Nullable final Marker marker, @Nullable final Throwable cause, final String descriptionFormat, final Object... descriptionArguments) {

        if (slf4j().isErrorEnabled()) {
            if (null == descriptionArguments || 0 == descriptionArguments.length) {
                slf4j().error( marker, descriptionFormat, cause );
            } else {
                slf4j().error( marker, String.format( descriptionFormat, descriptionArguments ), cause );
            }
        }

        return this;
    }

    /**
     * Log an application error.
     * <p/>
     * <p> This level is for all events that indicate failure to comply with the request. </p>
     *
     * @param cause                A throwable that details the stack at the time of this event.
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return Self, for chaining.
     */
    public Logger err(@Nullable final Throwable cause, final String descriptionFormat, final Object... descriptionArguments) {

        return err( null, cause, descriptionFormat, descriptionArguments );
    }

    /**
     * Log an application error.
     * <p/>
     * <p> This level is for all events that indicate failure to comply with the request. </p>
     *
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return Self, for chaining.
     */
    public Logger err(final String descriptionFormat, final Object... descriptionArguments) {

        return err( null, descriptionFormat, descriptionArguments );
    }

    /**
     * Log a user action.
     * <p/>
     * <p>This level is for all actions performed by a user that site administrators may later need to reflect upon to evaluate user
     * conduct.</p>
     *
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return Self, for chaining.
     */
    public Logger audit(final String descriptionFormat, final Object... descriptionArguments) {

        return inf( Markers.AUDIT, null, descriptionFormat, descriptionArguments );
    }

    /**
     * Log an internal inconsistency.
     * <p/>
     * <p> This level is for all events that occur unexpectedly. They indicate a bug in the application's flow. </p>
     *
     * @param cause A throwable that details the stack at the time of this event.
     *
     * @return The cause, if not null, wrapped in a {@link RuntimeException} if it isn't one.  For easy rethrowing.
     *
     * @see #bug(Throwable, String, Object...)
     */
    public RuntimeException bug(@NotNull final Throwable cause) {

        return checkNotNull( bug( checkNotNull( cause ), "Unexpected Error" ) );
    }

    /**
     * Log an internal inconsistency.
     * <p/>
     * <p> This level is for all events that occur unexpectedly. They indicate a bug in the application's flow. </p>
     *
     * @param cause                A throwable that details the stack at the time of this event.
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return The cause, if not null, wrapped in a {@link RuntimeException} if it isn't one.  For easy rethrowing.
     */
    @Nullable
    public RuntimeException bug(@Nullable final Throwable cause, final String descriptionFormat, final Object... descriptionArguments) {

        err( Markers.BUG, cause, descriptionFormat, descriptionArguments );

        return cause == null? null: Throwables.propagate( cause );
    }

    /**
     * Log an internal inconsistency.
     * <p/>
     * <p> This level is for all events that occur unexpectedly. They indicate a bug in the application's flow. </p>
     *
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     */
    public RuntimeException bug(final String descriptionFormat, final Object... descriptionArguments) {

        bug( null, descriptionFormat, descriptionArguments );

        return new RuntimeException( String.format( descriptionFormat, descriptionArguments ) );
    }

    /**
     * Log a security concern.
     * <p/>
     * <p> This level is for all events that should be evaluated by the security team. </p>
     *
     * @param cause                A throwable that details the stack at the time of this event.
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return Self, for chaining.
     */
    public Logger security(@Nullable final Throwable cause, final String descriptionFormat, final Object... descriptionArguments) {

        return wrn( Markers.SECURITY, cause, descriptionFormat, descriptionArguments );
    }

    /**
     * Log a security concern.
     * <p/>
     * <p> This level is for all events that should be evaluated by the security team. </p>
     *
     * @param descriptionFormat    The format of the event message. See {@link String#format(String, Object...)}.
     * @param descriptionArguments The arguments to inject into the event message format.
     *
     * @return Self, for chaining.
     */
    public Logger security(final String descriptionFormat, final Object... descriptionArguments) {

        return security( null, descriptionFormat, descriptionArguments );
    }

    // Internal operation --

    public org.slf4j.Logger slf4j() {

        if (logger == null) {
            logger = LoggerFactory.getLogger( name );
        }

        return logger;
    }

    private Logger(final String name) {

        this.name = name;
    }
}
