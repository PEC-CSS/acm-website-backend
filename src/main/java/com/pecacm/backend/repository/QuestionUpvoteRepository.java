package com.pecacm.backend.repository;

import com.pecacm.backend.entities.Question;
import com.pecacm.backend.entities.QuestionUpvote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionUpvoteRepository extends JpaRepository<QuestionUpvote, Integer> {

    Boolean existsByQuestionIdAndUserId(Integer questionId, Integer userId);

    // returns the rows removed so the caller can tell whether it actually won the
    // delete, instead of checking existence first and racing another request
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM QuestionUpvote qu " +
            "WHERE qu.question.id = :questionId AND qu.user.id = :userId")
    int deleteByQuestionIdAndUserId(@Param("questionId") Integer questionId, @Param("userId") Integer userId);

    void deleteAllByQuestion(Question question);

    // fetched for a whole page at once instead of one query per question
    @Query("SELECT qu.question.id FROM QuestionUpvote qu " +
            "WHERE qu.user.id = :userId AND qu.question.id IN :questionIds")
    List<Integer> findUpvotedQuestionIds(@Param("userId") Integer userId, @Param("questionIds") List<Integer> questionIds);
}
