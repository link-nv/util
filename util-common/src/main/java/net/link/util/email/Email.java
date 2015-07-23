package net.link.util.email;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
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
public class Email implements Runnable {

    private static final Logger logger = Logger.get( Email.class );

    private String               username;
    private String               password;
    private String               sender;
    private String               receiver;
    private String               subject;
    private String               text;
    private List<MailAttachment> attachments;
    private String               host;
    private int                  port;

    private String replyToAddress;

    public Email(String username, String password, String sender, String receiver, String subject, String text, String host, int port) {

        this( username, password, sender, receiver, subject, text, new ArrayList<MailAttachment>(), host, port );
    }

    public Email(String username, String password, String sender, String receiver, String subject, String text, MailAttachment attachment, String host,
                 int port) {

        this( username, password, sender, receiver, subject, text, attachment == null? new ArrayList<MailAttachment>(): Arrays.asList( attachment ), host,
                port );
    }

    public Email(String username, String password, String sender, String receiver, String subject, String text, List<MailAttachment> attachments, String host,
                 int port) {

        this.username = username;
        this.password = password;
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.text = text;
        this.attachments = attachments;
        this.host = host;
        this.port = port;

        this.replyToAddress = null;
    }

    public void setReplyToAddress(String replyToAddress) {

        this.replyToAddress = replyToAddress;
    }

    @Override
    public void run() {

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
            message.setSubject( subject );
            message.setSentDate( new Date() );

            Multipart multipart = new MimeMultipart();

            //Add message text
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent( text, "text/html; charset=utf-8" );
            multipart.addBodyPart( messageBodyPart );

            //Add attachements
            if (attachments != null) {

                for (MailAttachment attachment : attachments) {

                    if (attachment == null)
                        continue;

                    messageBodyPart = new MimeBodyPart();
                    DataSource source = new ByteArrayDataSource( attachment.getContent(), attachment.getMimeType() );
                    messageBodyPart.setDataHandler( new DataHandler( source ) );
                    messageBodyPart.setFileName( attachment.getFileName() );
                    multipart.addBodyPart( messageBodyPart );
                }
            }

            //Send
            message.setContent( multipart );
            Transport.send( message );

            logger.inf( "Mail has been sent to " + receiver );
        }
        catch (MessagingException ex) {

            logger.err( ex, "Failed to send email to %s", receiver );
        }
    }
}
