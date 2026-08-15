package com.pecacm.backend.services;

import com.pecacm.backend.entities.User;
import com.pecacm.backend.entities.VerificationToken;
import com.pecacm.backend.exception.AcmException;
import com.pecacm.backend.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.commonmark.node.Node;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

import org.commonmark.parser.Parser;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final VerificationService verificationService;
    private final UserRepository userRepository;
    private final UserService userService;

    @Value("${verify.base.frontend}")
    private String frontendBaseUrl;

    @Value("${verify.reset.path:forgot-password/change-password}")
    private String resetPath;

    // Gmail requires the sender to match the authenticated account, so the From
    // address is taken from the same property used to log in to the SMTP server.
    @Value("${spring.mail.username}")
    private String fromAddress;

    public EmailService(JavaMailSender javaMailSender, VerificationService verificationService, UserRepository userRepository, UserService userService) {
        this.javaMailSender = javaMailSender;
        this.verificationService = verificationService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public void sendVerificationEmail(String username) {
        if (!userRepository.checkVerifiedByEmail(username).orElse(false)) {
            throw new AcmException("Your email is not verified and hence we cannot change your password, please contact our admins", HttpStatus.BAD_REQUEST);
        }

        VerificationToken token = verificationService.getVerificationToken(userService.getUserByEmail(username));

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromAddress);
        mailMessage.setTo(username);
        mailMessage.setSubject("Reset your password");
        mailMessage.setText(
                "Hi,\n\n"
                        + "We received a request to reset the password for your PEC ACM account.\n\n"
                        + "Reset it here (this link is valid for 15 minutes):\n"
                        + buildResetLink(token) + "\n\n"
                        + "If you did not request this, you can safely ignore this email.\n"
        );

        javaMailSender.send(mailMessage);
    }

    private String buildResetLink(VerificationToken token) {
        String base = frontendBaseUrl.endsWith("/") ? frontendBaseUrl : frontendBaseUrl + "/";
        String path = resetPath.startsWith("/") ? resetPath.substring(1) : resetPath;
        return base + path + "?token=" + token.getToken();
    }

    public void sendEmail(List<User> users, String subject, String body) {
        String[] recipients = users.stream().map(User::getEmail).toArray(String[]::new);
        String htmlBody = convertMarkdownToHtml(body);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            message.setSubject(subject);
            MimeMessageHelper helper;
            helper = new MimeMessageHelper(message, true);
            helper.setTo(recipients);
            helper.setText(htmlBody, true);
            javaMailSender.send(message);
        } catch (MessagingException ex) {
            throw new AcmException("Not able to send mail");
        }
    }
    private String convertMarkdownToHtml(String markdownContent) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdownContent);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }
}