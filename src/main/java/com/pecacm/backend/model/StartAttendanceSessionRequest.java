package com.pecacm.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartAttendanceSessionRequest {
    private Integer eventId;
    private Integer durationMinutes;
    private Integer tokenExpirySeconds;
    private Integer gracePeriodSeconds;
    private Integer participantXp;
}
