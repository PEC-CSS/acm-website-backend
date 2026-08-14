package com.pecacm.backend.repository;

import com.pecacm.backend.entities.Question;
import com.pecacm.backend.entities.QuestionUpvote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionUpvoteRepository extends JpaRepository<QuestionUpvote, Integer> {

    Boolean existsByQuestionIdAndUserId(Integer questionId, Integer userId);

    void deleteByQuestionIdAndUserId(Integer questionId, Integer userId);

    void deleteAllByQuestion(Question question);

    // fetched for a whole page at once instead of one query per question
    @Query("SELECT qu.question.id FROM QuestionUpvote qu " +
            "WHERE qu.user.id = :userId AND qu.question.id IN :questionIds")
    List<Integer> findUpvotedQuestionIds(@Param("userId") Integer userId, @Param("questionIds") List<Integer> questionIds);
}
