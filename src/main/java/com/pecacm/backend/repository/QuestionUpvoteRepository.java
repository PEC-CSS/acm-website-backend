package com.pecacm.backend.repository;

import com.pecacm.backend.entities.Question;
import com.pecacm.backend.entities.QuestionUpvote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuestionUpvoteRepository extends JpaRepository<QuestionUpvote, Integer> {

    // returns 1 when the upvote was recorded and 0 when this user had already
    // upvoted, so a duplicate never has to be inferred from a constraint violation
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "INSERT INTO question_upvotes (question_id, user_id, created_at) " +
            "VALUES (:questionId, :userId, :createdAt) " +
            "ON CONFLICT (question_id, user_id) DO NOTHING", nativeQuery = true)
    int insertIfAbsent(@Param("questionId") Integer questionId, @Param("userId") Integer userId, @Param("createdAt") LocalDateTime createdAt);

    // returns the rows removed so the caller can tell whether it actually won the
    // delete, instead of checking existence first and racing another request
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM QuestionUpvote qu " +
            "WHERE qu.question.id = :questionId AND qu.user.id = :userId")
    int deleteByQuestionIdAndUserId(@Param("questionId") Integer questionId, @Param("userId") Integer userId);

    void deleteAllByQuestion(Question question);

    // counts and upvoted-by-me are both fetched a page at a time, not per question
    @Query("SELECT qu.question.id, COUNT(qu.id) FROM QuestionUpvote qu " +
            "WHERE qu.question.id IN :questionIds " +
            "GROUP BY qu.question.id")
    List<Object[]> countByQuestionIds(@Param("questionIds") List<Integer> questionIds);

    @Query("SELECT qu.question.id FROM QuestionUpvote qu " +
            "WHERE qu.user.id = :userId AND qu.question.id IN :questionIds")
    List<Integer> findUpvotedQuestionIds(@Param("userId") Integer userId, @Param("questionIds") List<Integer> questionIds);
}
