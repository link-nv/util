package net.link.util.email;

import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;


/**
 * User: gvhoecke <gianni.vanhoecke@lin-k.net>
 * Date: 18/02/14
 * Time: 13:50
 */
public class MailAttachment implements Serializable {

    private static final long serialVersionUID = -8201148905836579566L;

    private final String fileName;
    private final String mimeType;
    private final byte[] content;
    private final String contentID;

    public MailAttachment( byte[] content, String mimeType, String fileName ) {

        this.content = content;
        this.mimeType = mimeType;
        this.fileName = fileName;

        //Generate content ID
        this.contentID = String.format( "%s-%s", this.fileName, UUID.randomUUID().toString() );
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

    public String getContentID() {

        return contentID;
    }

    @Override
    public String toString() {

        return "MailAttachment{" +
                "fileName='" + fileName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", content=" + Arrays.toString(content) +
                ", contentID='" + contentID + '\'' +
                '}';
    }
}