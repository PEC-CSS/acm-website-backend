package com.pecacm.backend.entities;

import com.pecacm.backend.enums.AttendanceSessionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "attendance_sessions", schema = "public")
@Builder
public class AttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "secret_key", nullable = false)
    private String secretKey;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AttendanceSessionStatus status = AttendanceSessionStatus.ACTIVE;

    @Column(name = "created_by_id", nullable = false)
    private Integer createdById;

    @Column(name = "token_expiry_seconds", nullable = false)
    @Builder.Default
    private Integer tokenExpirySeconds = 5;

    @Column(name = "grace_period_seconds", nullable = false)
    @Builder.Default
    private Integer gracePeriodSeconds = 5;
}
