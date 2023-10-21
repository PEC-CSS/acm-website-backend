package com.pecacm.backend.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ForgetPasswordResponse {
    private String forgetPasswordToken;
}
