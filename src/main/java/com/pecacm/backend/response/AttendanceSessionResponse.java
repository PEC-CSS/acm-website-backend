package com.pecacm.backend.response;

import com.pecacm.backend.enums.AttendanceSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSessionResponse {
    private Integer id;
    private Integer eventId;
    private String eventTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AttendanceSessionStatus status;
    private Integer createdById;
    private Integer tokenExpirySeconds;
    private Integer gracePeriodSeconds;
    private Integer participantXp;
    private long totalAttendees;
}
