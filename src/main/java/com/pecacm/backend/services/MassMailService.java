package com.pecacm.backend.services;

import com.pecacm.backend.entities.Certificate;
import com.pecacm.backend.entities.Event;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class MassMailService {

    private final TemplateGeneratorService generatorService;
    private final ObjectMapper objectMapper;

    @Value("${spring.gmail-api.refresh-token}")
    private String refreshToken;

    @Value("${spring.gmail-api.user-email}")
    private String userEmail;

    private Gmail getGmailService() throws Exception {
        ClassPathResource resource = new ClassPathResource("secret.json");
        JsonNode root = objectMapper.readTree(resource.getInputStream());
        
        JsonNode secrets = root.has("web") ? root.get("web") : root.get("installed");
        if (secrets == null) throw new RuntimeException("Invalid secret.json file - No 'web' or 'installed' object found");

        String clientId = secrets.get("client_id").asText();
        String clientSecret = secrets.get("client_secret").asText();

        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();
        
        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("ACM Certification Tool")
                .build();
    }

    @Async
    public void sendCertificates(Event event, List<Certificate> certificates) {
        log.info("Starting Gmail API OAuth2 batch mass mail job for event: {}", event.getTitle());

        try {
            Gmail service = getGmailService();

            for (Certificate certificate : certificates) {
                try {
                    byte[] pdfBytes = generatorService.generateCertificatePdf(certificate);
                    MimeMessage mimeMessage = prepareMimeMessage(certificate.getRecipientEmail(), event.getTitle(), certificate.getRecipientName(), pdfBytes);
                    
                    Message message = createMessageWithEmail(mimeMessage);
                    service.users().messages().send("me", message).execute();
                    
                    log.info("Sent Gmail API message to: {}", certificate.getRecipientEmail());
                } catch (Exception e) {
                    log.error("Failed to send via Gmail API for: {}", certificate.getRecipientEmail(), e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to initialize Gmail API service", e);
        }
    }

    private MimeMessage prepareMimeMessage(String to, String eventName, String recipientName, byte[] pdfBytes) throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(userEmail));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject("Your Certificate for " + eventName);

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Dear " + recipientName + ",\n\nPlease find your participation certificate for " + eventName + " attached.\n\nBest regards,\nACM Team");

        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setContent(pdfBytes, "application/pdf");
        attachmentPart.setFileName("Certificate_" + eventName.replace(" ", "_") + ".pdf");

        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(attachmentPart);

        email.setContent(multipart);
        return email;
    }

    private Message createMessageWithEmail(MimeMessage emailContent) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().encodeToString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
}
