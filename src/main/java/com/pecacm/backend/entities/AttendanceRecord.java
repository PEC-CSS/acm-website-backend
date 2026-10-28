package com.pecacm.backend.entities;

import com.pecacm.backend.enums.AttendanceMethod;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "attendance_records",
        schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attendance_event_user", columnNames = {"event_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_attendance_records_session_id", columnList = "session_id"),
                @Index(name = "idx_attendance_records_event_xp", columnList = "event_id, xp_awarded")
        }
)
@Builder
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id", nullable = false)
    private AttendanceSession session;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "marked_at", nullable = false)
    private LocalDateTime markedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    @Builder.Default
    private AttendanceMethod method = AttendanceMethod.QR_SCAN;

    @Column(name = "marked_by_admin_id")
    private Integer markedByAdminId;

    @Column(name = "xp_awarded", nullable = false)
    @Builder.Default
    private Boolean xpAwarded = false;
}
