package com.pratham.mailsender.service;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.pratham.mailsender.model.EmailConfig;
import com.pratham.mailsender.model.EmailRequest;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendEmail(EmailConfig config, EmailRequest emailRequest) throws Exception {
        String accessToken = fetchAccessToken(config);
        Session session = buildMailSession(config, accessToken);
        Message message = buildMessage(session, config, emailRequest);
        Transport.send(message);
    }

    private ConfidentialClientApplication buildMsalClient(EmailConfig config) throws Exception {
        return ConfidentialClientApplication
            .builder(
                config.getClientId(),
                ClientCredentialFactory.createFromSecret(config.getClientSecret())
            )
            .authority("https://login.microsoftonline.com/" + config.getTenantId())
            .build();
    }

    private String fetchAccessToken(EmailConfig config) throws Exception {
        ConfidentialClientApplication app = buildMsalClient(config);
        ClientCredentialParameters params = ClientCredentialParameters
            .builder(Set.of("https://outlook.office365.com/.default"))
            .build();
        return app.acquireToken(params).get().accessToken();
    }

    private Session buildMailSession(EmailConfig config, String accessToken) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.office365.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth.mechanisms", "XOAUTH2");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getFromEmail(), accessToken);
            }
        });
    }

    private Message buildMessage(Session session, EmailConfig config, EmailRequest emailRequest) throws Exception {
        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(config.getFromEmail()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailRequest.getTo()));
        if (emailRequest.getCc() != null && !emailRequest.getCc().isBlank()) {
            message.setRecipients(Message.RecipientType.CC,
                InternetAddress.parse(emailRequest.getCc()));
        }

        if (emailRequest.getBcc() != null && !emailRequest.getBcc().isBlank()) {
            message.setRecipients(Message.RecipientType.BCC,
                InternetAddress.parse(emailRequest.getBcc()));
        }

        if (emailRequest.getReplyTo() != null && !emailRequest.getReplyTo().isBlank()) {
            message.setReplyTo(InternetAddress.parse(emailRequest.getReplyTo()));
        }

        message.setSubject(emailRequest.getSubject());
        message.setContent(emailRequest.getBody(), emailRequest.isHtml()
        ? "text/html; charset=utf-8"
        : "text/plain; charset=utf-8");

        return message;
    }
}
