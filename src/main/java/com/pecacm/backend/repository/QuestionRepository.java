package com.pecacm.backend.repository;

import com.pecacm.backend.entities.Question;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findAllByOrderByUpvotesDescCreatedDateDesc(PageRequest pageRequest);

    // locks the question row so that deleting it and adding an answer or upvote to
    // it cannot interleave, which would leave the delete failing on a foreign key
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM Question q WHERE q.id = :questionId")
    Optional<Question> findByIdForUpdate(@Param("questionId") Integer questionId);

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
