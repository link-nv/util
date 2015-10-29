package net.link.util.email;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * User: gvhoecke <gianni.vanhoecke@lin-k.net>
 * Date: 02/09/13
 * Time: 14:03
 */
public class Email implements Callable<Boolean>, Serializable {

    private static final Logger logger = Logger.get( Email.class );

    public static final String MIME_TYPE_HTML_MAIL = "text/html; charset=utf-8";

    private String               username;
    private String               password;
    private String               sender;
    private String               receiver;
    private String               subject;
    private String               htmlContent;
    private List<MailAttachment> attachments;
    private String               host;
    private int                  port;

    private String replyToAddress;

    public Email(String username, String password, String sender, String receiver, String subject, String htmlContent, String host, int port) {

        this( username, password, sender, receiver, subject, htmlContent, new ArrayList<MailAttachment>(), host, port );
    }

    public Email(String username, String password, String sender, String receiver, String subject, String htmlContent, @Nullable MailAttachment attachment,
                 String host, int port) {

        this( username, password, sender, receiver, subject, htmlContent,
                attachment == null? new ArrayList<MailAttachment>(): Collections.singletonList( attachment ), host, port );
    }

    public Email(String username, String password, String sender, String receiver, String subject, String htmlContent,
                 @NotNull List<MailAttachment> attachments, String host, int port) {

        this.username = username;
        this.password = password;
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.htmlContent = htmlContent;
        this.attachments = attachments;
        this.host = host;
        this.port = port;

        this.replyToAddress = null;
    }

    public void setReplyToAddress(String replyToAddress) {

        this.replyToAddress = replyToAddress;
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

            //Message
            MimeMessage message = new MimeMessage( session );
            message.setFrom( new InternetAddress( sender ) );
            message.setRecipients( Message.RecipientType.TO, new InternetAddress[] { new InternetAddress( receiver ) } );
            if (StringUtils.isNotBlank( this.replyToAddress )) {

                message.setReplyTo( new Address[] {

                        new InternetAddress( this.replyToAddress )
                } );
            }
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
            if (attachments != null) {

                for (MailAttachment attachment : attachments) {

                    if (attachment == null)
                        continue;

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

            return false;
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
               ", text='" + htmlContent + '\'' +
               ", attachments=" + attachments +
               ", host='" + host + '\'' +
               ", port=" + port +
               ", replyToAddress='" + replyToAddress + '\'' +
               '}';
    }
}

