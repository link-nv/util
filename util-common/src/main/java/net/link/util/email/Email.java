package net.link.util.email;

import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import net.link.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;


/**
 * User: gvhoecke <gianni.vanhoecke@lin-k.net>
 * Date: 02/09/13
 * Time: 14:03
 */
public class Email implements Callable<Boolean>, Serializable {

    private static final Logger logger = Logger.get( Email.class );

    public static final String MIME_TYPE_HTML_MAIL = "text/html; charset=utf-8";

    //Mandatory parameters
    private final String username;
    private final String password;
    private final String sender;
    private final String receiver;
    private final String subject;
    private final String htmlContent;
    private final String host;
    private final int    port;

    //Optional parameters
    private final String               senderAlias;
    private final String               bcc;
    private final String               cc;
    private final String               replyToAddress;
    private final List<MailAttachment> attachments;

    private Email(final Builder builder) {

        this.username = builder.username;
        this.password = builder.password;
        this.sender = builder.sender;
        this.receiver = builder.receiver;
        this.subject = builder.subject;
        this.htmlContent = builder.htmlContent;
        this.host = builder.host;
        this.port = builder.port;

        this.senderAlias = builder.senderAlias;
        this.bcc = builder.bcc;
        this.cc = builder.cc;
        this.replyToAddress = builder.replyToAddress;
        this.attachments = builder.attachments;
    }

    //Builder
    public static class Builder {

        //Mandatory parameters
        private final String username;
        private final String password;
        private final String sender;
        private final String receiver;
        private final String subject;
        private final String htmlContent;
        private final String host;
        private final int    port;

        //Optional parameters
        private String               senderAlias    = null;
        private String               bcc            = null;
        private String               cc             = null;
        private String               replyToAddress = null;
        private List<MailAttachment> attachments    = Lists.newArrayList();

        public Email build() {

            return new Email( this );
        }

        //Constructor with mandatory parameters
        public Builder(final String username, final String password, final String sender, final String receiver, final String subject, final String htmlContent,
                       final String host, final int port) {

            this.username = username;
            this.password = password;
            this.sender = sender;
            this.receiver = receiver;
            this.subject = subject;
            this.htmlContent = htmlContent;
            this.host = host;
            this.port = port;
        }

        public Builder senderAlias(final String senderAlias) {

            this.senderAlias = senderAlias;
            return this;
        }

        public Builder bcc(final String bcc) {

            this.bcc = bcc;
            return this;
        }

        public Builder cc(final String cc) {

            this.cc = cc;
            return this;
        }

        public Builder replyToAddress(final String replyToAddress) {

            this.replyToAddress = replyToAddress;
            return this;
        }

        /**
         * Adds an attachment, multiple withAttachment() are supported!
         */
        public Builder withAttachment(final MailAttachment mailAttachment) {

            if (mailAttachment != null && !this.attachments.contains( mailAttachment )) {

                this.attachments.add( mailAttachment );
            }
            return this;
        }
    }

    @Override
    public Boolean call()
            throws Exception {

        try {

            //Properties
            Properties mailProperties = new Properties();
            mailProperties.setProperty( "mail.smtp.host", host );
            mailProperties.setProperty( "mail.smtp.port", Integer.toString( port ) );
            mailProperties.setProperty( "mail.smtps.host", host );
            mailProperties.setProperty( "mail.smtps.port", Integer.toString( port ) );
            mailProperties.setProperty( "mail.smtp.starttls.enable", "true" );
            mailProperties.setProperty( "mail.smtp.EnableSSL.enable", "true" );
            mailProperties.setProperty( "mail.smtp.auth", "true" );
            mailProperties.setProperty( "mail.smtps.auth", "true" );

            //Required to avoid security exception.
            MailAuthenticator authentication = new MailAuthenticator( username, password );
            Session session = Session.getDefaultInstance( mailProperties, authentication );

            //Build message
            MimeMessage message = new MimeMessage( session );

            //Set alias, if any
            if (StringUtils.isNotBlank( senderAlias )) {

                message.setFrom( new InternetAddress( sender ) );
            } else {

                message.setFrom( new InternetAddress( sender, senderAlias ) );
            }

            //To
            message.setRecipients( Message.RecipientType.TO, new InternetAddress[] { new InternetAddress( receiver ) } );

            //Reply to
            if (StringUtils.isNotBlank( this.replyToAddress )) {

                message.setReplyTo( new Address[] { new InternetAddress( this.replyToAddress ) } );
            }

            //Bcc
            if (StringUtils.isNotBlank( bcc )) {

                message.setRecipients( Message.RecipientType.BCC, new InternetAddress[] { new InternetAddress( bcc ) } );
            }

            //Cc
            if (StringUtils.isNotBlank( cc )) {

                message.setRecipients( Message.RecipientType.CC, new InternetAddress[] { new InternetAddress( cc ) } );
            }

            //Data
            message.setSubject( subject, "UTF-8" );
            message.setSentDate( new Date() );

            //Cover wrap
            MimeBodyPart wrap = new MimeBodyPart();

            //Alternative text/html content
            MimeMultipart cover = new MimeMultipart( "alternative" );
            MimeBodyPart html = new MimeBodyPart();
            cover.addBodyPart( html );

            wrap.setContent( cover );

            MimeMultipart content = new MimeMultipart( "related" );
            message.setContent( content );
            content.addBodyPart( wrap );

            //Set mail content
            html.setContent( htmlContent, MIME_TYPE_HTML_MAIL );

            //Add attachments
            if (attachments != null && !attachments.isEmpty()) {

                for (MailAttachment attachment : attachments) {

                    if (attachment == null) {

                        continue;
                    }

                    MimeBodyPart messageBodyPart = new MimeBodyPart();
                    DataSource source = new ByteArrayDataSource( attachment.getContent(), attachment.getMimeType() );
                    messageBodyPart.setDataHandler( new DataHandler( source ) );
                    messageBodyPart.setFileName( attachment.getFileName() );
                    messageBodyPart.setContentID( String.format( "<%s>", attachment.getContentID() ) );
                    content.addBodyPart( messageBodyPart );
                }
            }

            //Send
            Transport.send( message );

            logger.inf( "Mail has been sent to " + receiver );

            return true;
        }
        catch (MessagingException ex) {

            logger.err( ex, "Failed to send email to %s", receiver );

            throw ex;
        }
    }

    @Override
    public String toString() {

        return "Email{" +
               "username='" + username + '\'' +
               ", password='" + String.format( "%" + password.length() + "s", "" ).replace( ' ', '*' ) + '\'' +
               ", sender='" + sender + '\'' +
               ", receiver='" + receiver + '\'' +
               ", subject='" + subject + '\'' +
               ", htmlContent='" + htmlContent + '\'' +
               ", host='" + host + '\'' +
               ", port=" + port +
               ", senderAlias='" + senderAlias + '\'' +
               ", bcc='" + bcc + '\'' +
               ", cc='" + cc + '\'' +
               ", replyToAddress='" + replyToAddress + '\'' +
               ", attachments=" + attachments +
               '}';
    }
}

