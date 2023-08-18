package com.pecacm.backend.services;

import com.pecacm.backend.entities.User;
import com.pecacm.backend.entities.VerificationToken;
import com.pecacm.backend.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final VerificationTokenRepository verificationTokenRepository;

    @Value("${verify.base.frontend}")
    private String hostname;

    @Autowired
    public EmailService(JavaMailSender javaMailSender, VerificationTokenRepository verificationTokenRepository) {
        this.javaMailSender = javaMailSender;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @Transactional
    public void sendVerificationEmail(User user) {
        VerificationToken token = verificationTokenRepository.save(
                VerificationToken.builder().user(user).build()
        );
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Email verification");
        mailMessage.setText(
                "Click on the link to verify your email: " + hostname + "verify?token=" + token.getToken()
        );
        javaMailSender.send(mailMessage);
    }
}
