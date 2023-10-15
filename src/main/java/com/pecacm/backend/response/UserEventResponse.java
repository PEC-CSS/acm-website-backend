package com.pecacm.backend.response;

import com.pecacm.backend.enums.EventRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Setter
@Getter
public class UserEventResponse {
    private Integer eventId;
    private String eventName;
    private EventRole eventRole;
    private Integer xp_gained;
    private LocalDateTime eventEnd;
}
