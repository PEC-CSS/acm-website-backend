package com.pecacm.backend.model;

import com.pecacm.backend.enums.Role;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {
    @Nullable
    private Role role;
    @Nullable
    private List<String> emails;
    private String subject;
    private String body;
}
