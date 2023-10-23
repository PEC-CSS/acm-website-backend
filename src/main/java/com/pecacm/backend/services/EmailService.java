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
}