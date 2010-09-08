package net.link.util.filter;

import net.link.util.performance.ProfileData;
import net.link.util.performance.ProfileDataLockedException;
import net.link.util.performance.ProfilingPolicyContextHandler;
import net.link.util.servlet.BufferedServletResponseWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.jms.server.messagecounter.MessageCounter;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


/**
 * Servlet Filter profiles the request and adds the results as headers of the response.<br>
 *
 * @author mbillemo
 */
public class ProfileFilter implements Filter {

    private static final Log LOG = LogFactory.getLog(ProfileFilter.class);
    private static MBeanServerConnection rmi;

    static {
        try {
            rmi = (MBeanServerConnection) getInitialContext().lookup("jmx/invoker/RMIAdaptor");
        } catch (NamingException e) {
            LOG.error("JMX unavailable.", e);
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Only attempt to profile HTTP requests.
        if (!(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        LOG.debug("Enabling profiler.");
        ProfileData profileData = new ProfileData();
        // publish the profile data on JACC
        ProfilingPolicyContextHandler.setProfileData(profileData);

        // Buffer the response so we can add our own headers.
        BufferedServletResponseWrapper responseWrapper = new BufferedServletResponseWrapper((HttpServletResponse) response);

        long startFreeMem = getFreeMemory();
        long startTime = System.currentTimeMillis();

        try {
            try {
                chain.doFilter(request, responseWrapper);
            }

            finally {
                long deltaTime = System.currentTimeMillis() - startTime;
                long endFreeMem = getFreeMemory();
                long auditSize = getAuditQueueSize();
                LOG.info("AUDIT QUEUE SIZE: " + auditSize);

                try {
                    profileData.addMeasurement(ProfileData.AUDIT_SIZE, auditSize);
                    profileData.addMeasurement(ProfileData.REQUEST_START_TIME, startTime);
                    profileData.addMeasurement(ProfileData.REQUEST_DELTA_TIME, deltaTime);
                    profileData.addMeasurement(ProfileData.REQUEST_START_FREE, startFreeMem);
                    profileData.addMeasurement(ProfileData.REQUEST_END_FREE, endFreeMem);
                } catch (ProfileDataLockedException e) {
                }

                // Add our profiling results as HTTP headers.
                for (Map.Entry<String, String> header : profileData.getHeaders().entrySet())
                    responseWrapper.addHeader(header.getKey(), header.getValue());

                if (profileData.isLocked()) {
                    LOG.debug("someone forgot to unlock the profile data");
                    profileData.unlock();
                }
            }
        }

        catch (Throwable e) {
            throw new ProfiledException(e, profileData.getHeaders());
        }

        finally {
            responseWrapper.commit();
        }
    }

    private long getAuditQueueSize() {

        try {
            @SuppressWarnings("unchecked")
            List<MessageCounter> queues = (List<MessageCounter>) rmi.getAttribute(new ObjectName("jboss.messaging:service=ServerPeer"),
                    "MessageCounters");

            try {
                for (MessageCounter queue : queues)
                    if (queue.getDestinationName().equals("Queue.auditBackend"))
                        return queue.getMessageCount();

                LOG.error("Audit queue not found.");
            } catch (Exception e) {
                LOG.error("Couldn't access audit queue stats in JMS queue counters.", e);
            }
        } catch (Exception e) {
            LOG.error("Failed to read in JMS queue counters through JMX.", e);
        }

        return -1;
    }

    private long getFreeMemory() {

        try {
            return (Long) rmi.getAttribute(new ObjectName("jboss.system:type=ServerInfo"), "FreeMemory");
        } catch (Exception e) {
            LOG.error("Failed to read in free memory through JMX.", e);
        }

        return -1;
    }

    private static InitialContext getInitialContext()
            throws NamingException {

        Hashtable<String, String> environment = new Hashtable<String, String>();

        environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        environment.put(Context.PROVIDER_URL, "localhost:1099");

        return new InitialContext(environment);
    }

    /**
     * {@inheritDoc}
     */
    public void init(@SuppressWarnings("unused") FilterConfig filterConfig) {

        // empty
    }

    /**
     * {@inheritDoc}
     */
    public void destroy() {

        // empty
    }
}
