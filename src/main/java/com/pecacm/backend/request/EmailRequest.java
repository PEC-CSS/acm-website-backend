package com.pecacm.backend.request;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EmailRequest {
    @Nullable
    private String role;
    @Nullable
    private List<String> emails;
    private String subject;
    private String body;
}
