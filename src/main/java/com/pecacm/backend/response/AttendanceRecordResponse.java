package com.pecacm.backend.response;

import com.pecacm.backend.enums.AttendanceMethod;
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
public class AttendanceRecordResponse {
    private Integer id;
    private Integer eventId;
    private Integer sessionId;
    private Integer userId;
    private String userName;
    private String userEmail;
    private Integer sid;
    private LocalDateTime markedAt;
    private AttendanceMethod method;
    private Integer markedByAdminId;
    private Boolean xpAwarded;
}
