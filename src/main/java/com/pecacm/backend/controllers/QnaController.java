package com.pecacm.backend.controllers;

import com.pecacm.backend.constants.Constants;
import com.pecacm.backend.entities.Answer;
import com.pecacm.backend.entities.Question;
import com.pecacm.backend.exception.AcmException;
import com.pecacm.backend.model.QnaRequest;
import com.pecacm.backend.services.QnaService;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class QnaController {

    private final QnaService qnaService;

    @Autowired
    public QnaController(QnaService qnaService) {
        this.qnaService = qnaService;
    }

    @PostMapping("/questions")
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<Question> createQuestion(@RequestBody QnaRequest qnaRequest) {
        Question question = qnaService.createQuestion(qnaRequest.getContent(), getLoggedInEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(question);
    }

    @GetMapping("/questions")
    @PreAuthorize(Constants.HAS_ANY_ROLE)
    public ResponseEntity<List<Question>> getQuestions(@RequestParam @Nullable Integer offset, @RequestParam @Nullable Integer pageSize) {
        if (offset == null) offset = 0;
        if (pageSize == null) pageSize = 20; // returning first 20 questions

        if (offset < 0) throw new AcmException("offset cannot be < 0", HttpStatus.BAD_REQUEST);
        if (pageSize <= 0) throw new AcmException("pageSize must be > 0", HttpStatus.BAD_REQUEST);

        return ResponseEntity.ok(qnaService.getQuestions(offset, pageSize, getLoggedInEmail()));
    }

    @GetMapping("/questions/{questionId}")
    @PreAuthorize(Constants.HAS_ANY_ROLE)
    public ResponseEntity<Question> getQuestion(@PathVariable Integer questionId) {
        return ResponseEntity.ok(qnaService.getQuestion(questionId, getLoggedInEmail()));
    }

    @PutMapping("/questions/{questionId}")
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<Question> updateQuestion(@PathVariable Integer questionId, @RequestBody QnaRequest qnaRequest) {
        return ResponseEntity.ok(qnaService.updateQuestion(questionId, qnaRequest.getContent(), getLoggedInEmail()));
    }

    @DeleteMapping("/questions/{questionId}")
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<Void> deleteQuestion(@PathVariable Integer questionId) {
        qnaService.deleteQuestion(questionId, getLoggedInEmail());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/questions/{questionId}/upvote")
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<Question> upvoteQuestion(@PathVariable Integer questionId) {
        return ResponseEntity.ok(qnaService.upvoteQuestion(questionId, getLoggedInEmail()));
    }

    @DeleteMapping("/questions/{questionId}/upvote")
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<Question> removeUpvote(@PathVariable Integer questionId) {
        return ResponseEntity.ok(qnaService.removeUpvote(questionId, getLoggedInEmail()));
    }

    @PostMapping("/questions/{questionId}/answers")
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<Answer> createAnswer(@PathVariable Integer questionId, @RequestBody QnaRequest qnaRequest) {
        Answer answer = qnaService.createAnswer(questionId, qnaRequest.getContent(), getLoggedInEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(answer);
    }

    @GetMapping("/questions/{questionId}/answers")
    @PreAuthorize(Constants.HAS_ANY_ROLE)
    public ResponseEntity<List<Answer>> getAnswersByQuestion(@PathVariable Integer questionId, @RequestParam @Nullable Integer offset, @RequestParam @Nullable Integer pageSize) {
        if (offset == null) offset = 0;
        if (pageSize == null) pageSize = 20; // returning first 20 answers

        if (offset < 0) throw new AcmException("offset cannot be < 0", HttpStatus.BAD_REQUEST);
        if (pageSize <= 0) throw new AcmException("pageSize must be > 0", HttpStatus.BAD_REQUEST);

        return ResponseEntity.ok(qnaService.getAnswersByQuestion(questionId, offset, pageSize, getLoggedInEmail()));
    }

    @PutMapping("/answers/{answerId}")
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<Answer> updateAnswer(@PathVariable Integer answerId, @RequestBody QnaRequest qnaRequest) {
        return ResponseEntity.ok(qnaService.updateAnswer(answerId, qnaRequest.getContent(), getLoggedInEmail()));
    }

    @DeleteMapping("/answers/{answerId}")
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<Void> deleteAnswer(@PathVariable Integer answerId) {
        qnaService.deleteAnswer(answerId, getLoggedInEmail());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // null for anonymous callers, so the read endpoints can skip the caller
    // specific flags without looking up a user that does not exist
    private String getLoggedInEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication.getName();
    }
}
