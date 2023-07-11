package com.pecacm.backend.model;

import com.pecacm.backend.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AssignRoleRequest {
    private String email;
    private Role newRole;
}
