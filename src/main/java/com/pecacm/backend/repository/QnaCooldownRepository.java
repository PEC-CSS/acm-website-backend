package com.pecacm.backend.repository;

import com.pecacm.backend.entities.QnaCooldown;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface QnaCooldownRepository extends JpaRepository<QnaCooldown, Integer> {

    // claims the next slot in one statement : inserts the first time the user posts
    // and afterwards only updates when the cooldown has elapsed, so simultaneous
    // requests cannot both get through. Returns 0 when the user must still wait.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "INSERT INTO qna_cooldowns (user_id, last_question_at) " +
            "VALUES (:userId, :now) " +
            "ON CONFLICT (user_id) DO UPDATE SET last_question_at = :now " +
            "WHERE qna_cooldowns.last_question_at IS NULL OR qna_cooldowns.last_question_at <= :cooldownStart", nativeQuery = true)
    int claimQuestionSlot(@Param("userId") Integer userId, @Param("now") LocalDateTime now, @Param("cooldownStart") LocalDateTime cooldownStart);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "INSERT INTO qna_cooldowns (user_id, last_answer_at) " +
            "VALUES (:userId, :now) " +
            "ON CONFLICT (user_id) DO UPDATE SET last_answer_at = :now " +
            "WHERE qna_cooldowns.last_answer_at IS NULL OR qna_cooldowns.last_answer_at <= :cooldownStart", nativeQuery = true)
    int claimAnswerSlot(@Param("userId") Integer userId, @Param("now") LocalDateTime now, @Param("cooldownStart") LocalDateTime cooldownStart);
}
