/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.ServletOutputStream;


/**
 * A buffered servlet output stream. The buffering happens in memory via a simple byte array output stream.
 *
 * @author fcorneli
 */
public class BufferedServletOutputStream extends ServletOutputStream {

    private final ByteArrayOutputStream buffer;

    public BufferedServletOutputStream() {

        buffer = new ByteArrayOutputStream();
    }

    @Override
    public void write(int b) {

        buffer.write( b );
    }

    @Override
    public void close()
            throws IOException {

        buffer.close();
        super.close();
    }

    @Override
    public void flush()
            throws IOException {

        buffer.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) {

        buffer.write( b, off, len );
    }

    @Override
    public void write(byte[] b)
            throws IOException {

        buffer.write( b );
    }

    /**
     * Gives back the data that this servlet output stream has been buffering.
     */
    public byte[] getData() {

        byte[] data = buffer.toByteArray();
        return data;
    }
}
