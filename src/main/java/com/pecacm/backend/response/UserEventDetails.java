package com.pecacm.backend.response;

import com.pecacm.backend.enums.EventRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class UserEventDetails {
    private Integer id;
    private String name;
    private EventRole role;
    private Integer xp_gained;
    private LocalDateTime timestamp;
}
