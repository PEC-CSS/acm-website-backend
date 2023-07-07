package com.pecacm.backend.services;

import com.pecacm.backend.entities.User;
import com.pecacm.backend.entities.VerificationToken;
import com.pecacm.backend.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailService {

    private JavaMailSender javaMailSender;
    private VerificationTokenRepository verificationTokenRepository;

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
        // TODO: url should not be hardcoded
        mailMessage.setText(
                "Click on the link to verify your email: http://localhost:8080/v1/user/verify?token=" + token.getToken()
        );
        javaMailSender.send(mailMessage);
    }
}