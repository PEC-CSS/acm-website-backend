package com.pecacm.backend.entities;

import com.pecacm.backend.enums.Branch;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="events", schema = "public")
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    private Branch branch;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name="description", nullable = false)
    private String description;

    @Column(name = "attendance_sheet")
    private String attendance;

    @Column(name = "venue")
    private String venue;

    @Column(name = "ended")
    private boolean ended = false;
}
