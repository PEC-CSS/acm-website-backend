package com.pecacm.backend.controllers;

import com.pecacm.backend.constants.Constants;
import com.pecacm.backend.entities.User;
import com.pecacm.backend.model.EmailRequest;
import com.pecacm.backend.services.EmailService;
import com.pecacm.backend.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/email")
public class EmailController {
    private final EmailService emailService;
    private final UserService userService;

    public EmailController(EmailService emailService, UserService userService) {
        this.emailService = emailService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize(Constants.HAS_ROLE_CORE_AND_ABOVE)
    public ResponseEntity<String> sendBulkEmail(@RequestBody EmailRequest emailRequest) {
        List<User> users;

        if (emailRequest.getEmails() != null) {
            users = userService.getUserByEmailIds(emailRequest.getEmails());
        } else if (emailRequest.getRole() != null) {
            users = userService.getUserByRole(emailRequest.getRole());
        } else {
            users = userService.getAllUsers();
        }

        emailService.sendEmail(users, emailRequest.getSubject(), emailRequest.getBody());
        return ResponseEntity.ok("Mails sent.");
    }
}
