package com.pecacm.backend.controllers;

import com.pecacm.backend.constants.Constants;
import com.pecacm.backend.entities.User;
import com.pecacm.backend.enums.Role;
import com.pecacm.backend.model.EmailRequest;
import com.pecacm.backend.services.EmailService;
import com.pecacm.backend.services.UserService;
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

    @PostMapping("/by-role")
    @PreAuthorize(Constants.HAS_ROLE_CORE_AND_ABOVE)
    public void sendEmailByRole(@RequestBody EmailRequest emailRequest) {
        List<User> users = userService.getUserByRole(emailRequest.getRole());
        emailService.sendEmail(users,emailRequest.getSubject(),emailRequest.getBody());
    }

    @PostMapping("/by-user-ids")
    @PreAuthorize(Constants.HAS_ROLE_CORE_AND_ABOVE)
    public void sendEmailByUserIds(@RequestBody EmailRequest emailRequest) {
        List<User> users = userService.getUserByEmailIds(emailRequest.getEmails());
        System.out.println(users);
        emailService.sendEmail(users,emailRequest.getSubject(),emailRequest.getBody());
    }

    @PostMapping("/everyone")
    @PreAuthorize(Constants.HAS_ROLE_CORE_AND_ABOVE)
    public void sendEmailToEveryone(@RequestBody EmailRequest emailRequest) {
        List<User> users = userService.getAllUsers();
        emailService.sendEmail(users,emailRequest.getSubject(),emailRequest.getBody());
    }
}
