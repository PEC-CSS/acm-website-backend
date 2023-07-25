package com.pecacm.backend.controllers;

import com.pecacm.backend.constants.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/health")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(Constants.HAS_ANY_ROLE)
    public String health() {
        return "Health OK";
    }
}
