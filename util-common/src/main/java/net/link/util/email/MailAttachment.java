package net.link.util.email;

import java.io.Serializable;


/**
 * User: gvhoecke <gianni.vanhoecke@lin-k.net>
 * Date: 18/02/14
 * Time: 13:50
 */
public class MailAttachment implements Serializable {

    private byte[] content;
    private String mimeType;
    private String fileName;

    public MailAttachment(byte[] content, String mimeType, String fileName) {

        this.content = content;
        this.mimeType = mimeType;
        this.fileName = fileName;
    }

    public byte[] getContent() {

        return content;
    }

    public String getMimeType() {

        return mimeType;
    }

    public String getFileName() {

        return fileName;
    }
}