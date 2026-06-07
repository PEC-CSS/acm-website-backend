package com.pecacm.backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.pecacm.backend.enums.Branch;

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

    @Column(name="description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "related_link")
    @Builder.Default
    private String relatedLink = "";

    @Column(name = "venue")
    private String venue;

    @Column(name = "ended")
    @Builder.Default
    private boolean ended = false;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private Template template;
}
