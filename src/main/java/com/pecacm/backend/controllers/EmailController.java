package com.pecacm.backend.controllers;

import com.pecacm.backend.entities.User;
import com.pecacm.backend.enums.Role;
import com.pecacm.backend.request.EmailRequest;
import com.pecacm.backend.services.EmailService;
import com.pecacm.backend.services.UserService;
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
    public void sendEmailByRole(@RequestBody EmailRequest emailRequest) {
        List<User> users = userService.getUserByRole(Role.valueOf(emailRequest.getRole()));
        emailService.sendEmail(users,emailRequest.getSubject(),emailRequest.getBody());
    }

    @PostMapping("/by-user-ids")
    public void sendEmailByUserIds(@RequestBody EmailRequest emailRequest) {
        List<User> users = userService.getUserByEmailIds(emailRequest.getEmails());
        System.out.println(users);
        emailService.sendEmail(users,emailRequest.getSubject(),emailRequest.getBody());
    }

    @PostMapping("/everyone")
    public void sendEmailToEveryone(@RequestBody EmailRequest emailRequest) {
        List<User> users = userService.getAllUsers();
        emailService.sendEmail(users,emailRequest.getSubject(),emailRequest.getBody());
    }
}
