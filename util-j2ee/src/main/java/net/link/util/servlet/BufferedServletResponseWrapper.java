/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.util.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Buffered servlet response wrapper.
 *
 * <p>
 * See also: Servlet API version 2.4 specifications.
 * </p>
 *
 * @author fcorneli
 */
public class BufferedServletResponseWrapper extends HttpServletResponseWrapper {

    private static final Log LOG = LogFactory.getLog( BufferedServletResponseWrapper.class );

    private final HttpServletResponse origResponse;

    private final BufferedServletOutputStream bufferedServletOutputStream;

    private PrintWriter writer;

    public BufferedServletResponseWrapper(HttpServletResponse response) {

        super( response );
        origResponse = response;
        bufferedServletOutputStream = new BufferedServletOutputStream();
    }

    /**
     * This method will commit the buffered response to the real output response.
     *
     * @throws IOException
     */
    public void commit()
            throws IOException {

        if (null != writer)
            /*
             * We need to flush the writer first so that the buffered servlet output stream holds all the data.
             */
            writer.flush();
        byte[] data = bufferedServletOutputStream.getData();
        LOG.debug( "committing " + data.length + " bytes" );
        IOUtils.write( data, origResponse.getOutputStream() );
    }

    @Override
    public ServletOutputStream getOutputStream() {

        LOG.debug( "get output stream" );
        return bufferedServletOutputStream;
    }

    @Override
    public PrintWriter getWriter()
            throws IOException {

        LOG.debug( "get writer" );
        if (null == writer) {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter( bufferedServletOutputStream, getCharacterEncoding() );
            writer = new PrintWriter( outputStreamWriter );
        }
        return writer;
    }
}
