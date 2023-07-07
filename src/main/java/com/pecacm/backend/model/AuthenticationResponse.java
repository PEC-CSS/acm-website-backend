package com.pecacm.backend.model;

import com.pecacm.backend.entities.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthenticationResponse {
    private String jwtToken;
    private User user;
}
