package net.link.util.jpa.osiv;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import net.link.util.logging.Logger;


/**
 * User: gvhoecke <gianni.vanhoecke@lin-k.net>
 * Date: 02/09/13
 * Time: 13:41
 */
public class PersistenceFilter implements Filter {

    private static final Logger logger = Logger.get( PersistenceFilter.class );

    /**
     * Called by the web container to indicate to a filter that it is being placed into
     * service. The servlet container calls the init method exactly once after instantiating the
     * filter. The init method must complete successfully before the filter is asked to do any
     * filtering work. <br><br>
     * The web container cannot place the filter into service if the init method either<br>
     * 1.Throws a ServletException <br>
     * 2.Does not return within a time period defined by the web container
     */
    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException {

        //Empty
    }

    /**
     * The <code>doFilter</code> method of the Filter is called by the container
     * each time a request/response pair is passed through the chain due
     * to a client request for a resource at the end of the chain. The FilterChain passed in to this
     * method allows the Filter to pass on the request and response to the next entity in the
     * chain.<p>
     * A typical implementation of this method would follow the following pattern:- <br>
     * 1. Examine the request<br>
     * 2. Optionally wrap the request object with a custom implementation to
     * filter content or headers for input filtering <br>
     * 3. Optionally wrap the response object with a custom implementation to
     * filter content or headers for output filtering <br>
     * 4. a) <strong>Either</strong> invoke the next entity in the chain using the FilterChain object (<code>chain.doFilter()</code>), <br>
     * 4. b) <strong>or</strong> not pass on the request/response pair to the next entity in the filter chain to block the request processing<br>
     * 5. Directly set headers on the response after invocation of the next entity in ther filter chain.
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        try {

            filterChain.doFilter( servletRequest, servletResponse );

            logger.dbg( "commiting" );
            DataHandler.commit();
        }
        catch (IOException e) {

            logger.err( e, "rollback" );
            DataHandler.rollback();

            throw e;
        }
        catch (ServletException e) {

            logger.err( "rollback" );
            DataHandler.rollback();

            throw e;
        }
        catch (Exception e) {

            logger.err( "rollback" );
            DataHandler.rollback();

            throw new ServletException( e );
        }
    }

    /**
     * Called by the web container to indicate to a filter that it is being taken out of service. This
     * method is only called once all threads within the filter's doFilter method have exited or after
     * a timeout period has passed. After the web container calls this method, it will not call the
     * doFilter method again on this instance of the filter. <br><br>
     * <p/>
     * This method gives the filter an opportunity to clean up any resources that are being held (for
     * example, memory, file handles, threads) and make sure that any persistent state is synchronized
     * with the filter's current state in memory.
     */
    @Override
    public void destroy() {

        //Empty
    }
}
