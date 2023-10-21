package com.pecacm.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "forget_password_token")
public class ForgetPasswordToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "token")
    private UUID token;

    @OneToOne(targetEntity = User.class,fetch = FetchType.EAGER)
    @JoinColumn(name = "id",nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;
}

