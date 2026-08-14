package com.pecacm.backend.repository;

import com.pecacm.backend.entities.Question;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findAllByOrderByUpvotesDescCreatedDateDesc(PageRequest pageRequest);

    // updated in one statement so that concurrent upvotes are not lost
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Question " +
            "SET upvotes = upvotes + 1 " +
            "WHERE id = :questionId")
    void incrementUpvotes(@Param("questionId") Integer questionId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Question " +
            "SET upvotes = upvotes - 1 " +
            "WHERE id = :questionId AND upvotes > 0")
    void decrementUpvotes(@Param("questionId") Integer questionId);
}
