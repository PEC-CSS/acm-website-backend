package com.pecacm.backend.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="attendance", schema = "public")
@Builder
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name =  "id", unique = true, nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "eventId")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @Column(name = "role")
    // TODO: Change name

    private String eventRole;

    @Column(name = "status")
    private String status;

    @Column(name = "feedback")
    private String feedback;
}