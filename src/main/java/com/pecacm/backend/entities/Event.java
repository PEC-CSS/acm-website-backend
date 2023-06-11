package com.pecacm.backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.security.Timestamp;

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

    @Column(name = "name")
    private String name;

    @Column(name = "subgroup")
    private String subgroup;

    @Column(name = "date")
    private Timestamp date;

    @Column(name = "participants_sheet")
    private String participants;

    @Column(name="detail")
    private String detail;

    @Column(name = "attendance_sheet")
    private String attendance;
}
