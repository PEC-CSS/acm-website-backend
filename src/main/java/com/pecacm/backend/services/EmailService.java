package com.pecacm.backend.services;

import com.pecacm.backend.entities.User;
import com.pecacm.backend.entities.VerificationToken;
import com.pecacm.backend.exception.AcmException;
import com.pecacm.backend.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.commonmark.node.Node;
import org.commonmark.renderer.html.HtmlRenderer;
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
        mailMessage.setTo(username);
        mailMessage.setSubject("Reset your password");
        mailMessage.setText(
                "to change your password please click here " + "dummyFrontEndRoute?token=" + token.getToken().toString()
        );

        javaMailSender.send(mailMessage);
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