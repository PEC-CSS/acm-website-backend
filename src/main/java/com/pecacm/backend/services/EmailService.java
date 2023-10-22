package com.pecacm.backend.services;

import com.pecacm.backend.entities.VerificationToken;
import com.pecacm.backend.exception.AcmException;
import com.pecacm.backend.repository.VerificationTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final VerificationTokenRepository verificationTokenRepository;
    public EmailService(JavaMailSender javaMailSender, VerificationTokenRepository verificationTokenRepository) {
        this.javaMailSender = javaMailSender;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    public void sendVerificationEmail(String username) {
        VerificationToken token = verificationTokenRepository.findByUsername(username).orElse(null);
        if (token == null) {
            throw new AcmException("User with provided email does not exist", HttpStatus.NOT_FOUND);
        }
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(username);
        mailMessage.setSubject("Reset your password");
        mailMessage.setText(
                token.getToken().toString()
        );

        javaMailSender.send(mailMessage);
    }
}