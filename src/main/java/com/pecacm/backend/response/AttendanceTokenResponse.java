package com.pecacm.backend.response;

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
public class AttendanceTokenResponse {
    private String token;
    private Integer sessionId;
    private Integer eventId;
    private Long expiresInSeconds;
    private Integer windowSeconds;
    private Long serverTimeEpochMillis;
    private Long remainingMillisInWindow;
}
