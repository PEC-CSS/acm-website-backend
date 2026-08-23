package com.pecacm.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "questions", schema = "public")
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;


    // questions are anonymous, asker is kept only for the edit/delete checks.
    // lazy because it is never serialized, only its id is compared
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User askedBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // none of these are persisted, they are derived per request : the count comes
    // from the upvote rows, the flags from who is asking
    @Builder.Default
    @Transient
    private Integer upvotes = 0;

    @Builder.Default
    @Transient
    private Boolean upvoted = false;

    @Builder.Default
    @Transient
    private Boolean owned = false;
}
