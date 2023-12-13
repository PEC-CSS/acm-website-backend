package com.pecacm.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pecacm.backend.enums.EventRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="transactions", schema = "public")
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @Enumerated(EnumType.STRING)
    private EventRole role = EventRole.PARTICIPANT;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "xp_awarded")
    private Integer xp;
}
