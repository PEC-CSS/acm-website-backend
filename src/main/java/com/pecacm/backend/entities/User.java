package com.pecacm.backend.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", schema = "public")
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name="email", unique = true)
    private String email;

    @Column(name="password")
    private String password;

    @Column(name="batch")
    private Integer batch;

    @Column(name = "branch")
    private String branch;

    @Column(name="display_picture")
    private String dp;

    @Column(name="student_id")
    private Integer sid;

    @Column(name = "verified")
    private Boolean verified;

    @Column(name="designation")
    private String designation;

    @Column(name="xp_total")
    private Integer xp;
}
