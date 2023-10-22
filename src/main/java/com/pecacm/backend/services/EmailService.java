package com.pecacm.backend.services;

import com.pecacm.backend.entities.VerificationToken;
import com.pecacm.backend.exception.AcmException;
import com.pecacm.backend.repository.UserRepository;
import com.pecacm.backend.repository.VerificationTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final VerificationTokenRepository verificationTokenRepository;

    private final UserRepository userRepository;
    public EmailService(JavaMailSender javaMailSender, VerificationTokenRepository verificationTokenRepository, UserRepository userRepository) {
        this.javaMailSender = javaMailSender;
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
    }

    public void sendVerificationEmail(String username) {
        VerificationToken token = verificationTokenRepository.findByUsername(username).orElse(null);
        if (token == null) {
            throw new AcmException("User with provided email does not exist", HttpStatus.NOT_FOUND);
        }
        if (!userRepository.checkVerifiedByEmail(username).orElse(false)) {
            throw new AcmException("Your email is not verified and hence we cannot change your password, please contact our admins", HttpStatus.BAD_REQUEST);
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