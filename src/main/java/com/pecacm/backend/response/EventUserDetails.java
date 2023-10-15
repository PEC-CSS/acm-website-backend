package com.pecacm.backend.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EventUserDetails {
    private Integer id;
    private String email;
    private String name;
    private String dp;
}
