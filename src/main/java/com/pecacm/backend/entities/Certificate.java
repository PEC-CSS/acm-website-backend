package com.pecacm.backend.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "certificates")
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Column(name = "certificate_url")
    private String certificateUrl;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "issue_date")
    private String issueDate;
}
