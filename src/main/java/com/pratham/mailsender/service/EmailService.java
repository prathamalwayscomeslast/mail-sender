package com.pratham.mailsender.service;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
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
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendEmail(EmailConfig config, EmailRequest emailRequest) throws Exception {
        System.out.println("SEND EMAIL START");
        String accessToken = fetchAccessToken(config);
        Session session = buildMailSession(config, accessToken);
        Message message = buildMessage(session, config, emailRequest);
        System.out.println("[5] Calling Transport.send()...");
        Transport.send(message);
        System.out.println("[5] Email sent");
        System.out.println("SEND EMAIL END");
    }

    private ConfidentialClientApplication buildMsalClient(EmailConfig config) throws Exception {
        System.out.println("[1] Building MSAL client...");
        System.out.println("    clientId  : " + config.getClientId());
        System.out.println("    tenantId  : " + config.getTenantId());
        System.out.println("    secret    : " + (config.getClientSecret() != null ? "***SET***" : "NULL"));

        ConfidentialClientApplication app = ConfidentialClientApplication
            .builder(
                config.getClientId(),
                ClientCredentialFactory.createFromSecret(config.getClientSecret())
            )
            .authority("https://login.microsoftonline.com/" + config.getTenantId())
            .build();

        System.out.println("[1] MSAL client built");
        return app;
    }

    private String fetchAccessToken(EmailConfig config) throws Exception {
        System.out.println("[2] Fetching OAuth2 token from Azure AD...");

        ConfidentialClientApplication app = buildMsalClient(config);

        ClientCredentialParameters params = ClientCredentialParameters
            .builder(Set.of("https://outlook.office365.com/.default"))
            .build();

        System.out.println("[2] Calling Azure AD token endpoint...");
        IAuthenticationResult result = app.acquireToken(params)
            .get(10, TimeUnit.SECONDS);

        System.out.println("[2] Token fetched. Expires: " + result.expiresOnDate());
        return result.accessToken();
    }

    private Session buildMailSession(EmailConfig config, String accessToken) {
        System.out.println("[3] Building mail session...");

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.office365.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        System.out.println("[3] Mail session built");
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                System.out.println("[3] Authenticating via XOAUTH2...");
                return new PasswordAuthentication(config.getFromEmail(), accessToken);
            }
        });
    }

    private Message buildMessage(Session session, EmailConfig config, EmailRequest req) throws Exception {
        System.out.println("[4] Building email message...");
        System.out.println("    from    : " + config.getFromEmail());
        System.out.println("    to      : " + req.getTo());
        System.out.println("    cc      : " + req.getCc());
        System.out.println("    bcc     : " + req.getBcc());
        System.out.println("    subject : " + req.getSubject());

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(config.getFromEmail()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(req.getTo()));

        if (req.getCc() != null && !req.getCc().isBlank())
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(req.getCc()));

        if (req.getBcc() != null && !req.getBcc().isBlank())
            message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(req.getBcc()));

        if (req.getReplyTo() != null && !req.getReplyTo().isBlank())
            message.setReplyTo(InternetAddress.parse(req.getReplyTo()));

        message.setSubject(req.getSubject());
        message.setContent(req.getBody(), req.isHtml()
            ? "text/html; charset=utf-8"
            : "text/plain; charset=utf-8");

        System.out.println("[4] Message built");
        return message;
    }
}
