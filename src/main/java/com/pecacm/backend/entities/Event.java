package com.pecacm.backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

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

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "branch")
    private String branch;

    @Column(name = "date")
    private Timestamp date;

    @Column(name="detail", nullable = false)
    private String detail;

    @Column(name = "attendance_sheet")
    private String attendance;

    @Column(name = "event_status", nullable = false)
    private String eventStatus;
}
