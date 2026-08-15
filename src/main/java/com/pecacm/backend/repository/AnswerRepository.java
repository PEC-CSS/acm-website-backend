package com.pecacm.backend.repository;

import com.pecacm.backend.entities.Answer;
import com.pecacm.backend.entities.Question;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer> {

    List<Answer> findAllByQuestionIdOrderByCreatedAtAsc(Integer questionId, PageRequest pageRequest);

    void deleteAllByQuestion(Question question);
}
