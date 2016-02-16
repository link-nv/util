package net.link.util.email;

import java.util.List;


public class EmailBuilder {

    private String username;
    private String               password;
    private String               sender;
    private String               receiver;
    private String               subject;
    private String               htmlContent;
    private String               host;
    private int                  port;
    private List<MailAttachment> attachments = attachment == null? new ArrayList<MailAttachment>(): Collections.singletonList( attachment );
    private MailAttachment attachment;

    public EmailBuilder setUsername(final String username) {

        this.username = username;
        return this;
    }

    public EmailBuilder setPassword(final String password) {

        this.password = password;
        return this;
    }

    public EmailBuilder setSender(final String sender) {

        this.sender = sender;
        return this;
    }

    public EmailBuilder setReceiver(final String receiver) {

        this.receiver = receiver;
        return this;
    }

    public EmailBuilder setSubject(final String subject) {

        this.subject = subject;
        return this;
    }

    public EmailBuilder setHtmlContent(final String htmlContent) {

        this.htmlContent = htmlContent;
        return this;
    }

    public EmailBuilder setHost(final String host) {

        this.host = host;
        return this;
    }

    public EmailBuilder setPort(final int port) {

        this.port = port;
        return this;
    }

    public EmailBuilder setAttachments(final List<MailAttachment> attachments) {

        this.attachments = attachments;
        return this;
    }

    public EmailBuilder setAttachment(final MailAttachment attachment) {

        this.attachment = attachment;
        return this;
    }

    public Email createEmail() {

        return new Email( username, password, sender, receiver, subject, htmlContent, attachments, host, port );
    }
}