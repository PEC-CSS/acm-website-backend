package com.pecacm.backend.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="sponsors", schema = "public")
@Builder
public class Sponsor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "website")
    private String website;

    @Column(name = "display_logo")
    private String logo;

    @Column(name = "sponsorship")
    private String sponsorship;
}
