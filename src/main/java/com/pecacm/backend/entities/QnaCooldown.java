package com.pecacm.backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// one row per user, holding when they last posted. Kept out of the User entity so
// the Q&A feature owns its own state, and kept off the posts themselves so that
// deleting a post cannot reset the cooldown
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "qna_cooldowns", schema = "public")
@Builder
public class QnaCooldown {

    @Id
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "last_question_at")
    private LocalDateTime lastQuestionAt;

    @Column(name = "last_answer_at")
    private LocalDateTime lastAnswerAt;
}
