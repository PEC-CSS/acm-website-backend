package com.pecacm.backend.services;

import com.pecacm.backend.constants.Constants;
import com.pecacm.backend.constants.ErrorConstants;
import com.pecacm.backend.entities.Answer;
import com.pecacm.backend.entities.Question;
import com.pecacm.backend.entities.User;
import com.pecacm.backend.exception.AcmException;
import com.pecacm.backend.repository.AnswerRepository;
import com.pecacm.backend.repository.QuestionRepository;
import com.pecacm.backend.repository.QuestionUpvoteRepository;
import com.pecacm.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class QnaService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuestionUpvoteRepository questionUpvoteRepository;
    private final UserRepository userRepository;

    public QnaService(QuestionRepository questionRepository, AnswerRepository answerRepository, QuestionUpvoteRepository questionUpvoteRepository, UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.questionUpvoteRepository = questionUpvoteRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Question createQuestion(String content, String email) {
        validateContent(content);
        User user = getVerifiedUser(email);
        verifyQuestionCooldown(user);

        Question question = Question.builder()
                .content(content)
                .askedBy(user)
                .build();
        return markCallerState(questionRepository.save(question), email);
    }

    public List<Question> getQuestions(Integer offset, Integer pageSize, String email) {
        List<Question> questions = questionRepository.findAllByOrderByUpvotesDescCreatedDateDesc(PageRequest.of(offset, pageSize));
        return markCallerState(questions, email);
    }

    public Question getQuestion(Integer questionId) {
        return questionRepository.findById(questionId).orElseThrow(() ->
                new AcmException(ErrorConstants.QUESTION_NOT_FOUND + questionId, HttpStatus.NOT_FOUND)
        );
    }

    private Question getQuestionForUpdate(Integer questionId) {
        return questionRepository.findByIdForUpdate(questionId).orElseThrow(() ->
                new AcmException(ErrorConstants.QUESTION_NOT_FOUND + questionId, HttpStatus.NOT_FOUND)
        );
    }

    public Question getQuestion(Integer questionId, String email) {
        return markCallerState(getQuestion(questionId), email);
    }

    public Question updateQuestion(Integer questionId, String content, String email) {
        validateContent(content);
        Question question = getQuestion(questionId);
        verifyAskedBy(question, email);

        question.setContent(content);
        return markCallerState(questionRepository.save(question), email);
    }

    // upvotes and answers go first, they reference the question being removed
    @Transactional
    public void deleteQuestion(Integer questionId, String email) {
        Question question = getQuestionForUpdate(questionId);
        verifyAskedBy(question, email);

        questionUpvoteRepository.deleteAllByQuestion(question);
        answerRepository.deleteAllByQuestion(question);
        questionRepository.delete(question);
    }

    @Transactional
    public Question upvoteQuestion(Integer questionId, String email) {
        getQuestionForUpdate(questionId);
        User user = getVerifiedUser(email);

        // the insert itself decides the outcome, so a duplicate is reported without
        // having to interpret a constraint violation that may mean something else
        if (questionUpvoteRepository.insertIfAbsent(questionId, user.getId(), LocalDateTime.now()) == 0) {
            throw new AcmException(ErrorConstants.QUESTION_ALREADY_UPVOTED, HttpStatus.CONFLICT);
        }
        questionRepository.incrementUpvotes(questionId);

        return markCallerState(getQuestion(questionId), email);
    }

    @Transactional
    public Question removeUpvote(Integer questionId, String email) {
        getQuestion(questionId);
        User user = getVerifiedUser(email);

        // the delete itself decides the outcome, so two concurrent removals cannot
        // both decrement the counter for a single upvote row
        if (questionUpvoteRepository.deleteByQuestionIdAndUserId(questionId, user.getId()) == 0) {
            throw new AcmException(ErrorConstants.QUESTION_NOT_UPVOTED, HttpStatus.CONFLICT);
        }
        questionRepository.decrementUpvotes(questionId);

        return markCallerState(getQuestion(questionId), email);
    }

    @Transactional
    public Answer createAnswer(Integer questionId, String content, String email) {
        validateContent(content);
        User user = getVerifiedUser(email);
        // question is resolved before the cooldown so that answering a question
        // that does not exist reports that, rather than the rate limit
        Question question = getQuestionForUpdate(questionId);
        verifyAnswerCooldown(user);

        Answer answer = Answer.builder()
                .content(content)
                .question(question)
                .answeredBy(user)
                .build();
        return markOwned(answerRepository.save(answer), email);
    }

    public List<Answer> getAnswersByQuestion(Integer questionId, Integer offset, Integer pageSize, String email) {
        getQuestion(questionId);
        List<Answer> answers = answerRepository.findAllByQuestionIdOrderByCreatedDateAsc(questionId, PageRequest.of(offset, pageSize));
        return markOwned(answers, email);
    }

    public Answer updateAnswer(Integer answerId, String content, String email) {
        validateContent(content);
        Answer answer = getAnswer(answerId);
        verifyAnsweredBy(answer, email);

        answer.setContent(content);
        return markOwned(answerRepository.save(answer), email);
    }

    public void deleteAnswer(Integer answerId, String email) {
        Answer answer = getAnswer(answerId);
        verifyAnsweredBy(answer, email);

        answerRepository.delete(answer);
    }

    private Answer getAnswer(Integer answerId) {
        return answerRepository.findById(answerId).orElseThrow(() ->
                new AcmException(ErrorConstants.ANSWER_NOT_FOUND + answerId, HttpStatus.NOT_FOUND)
        );
    }

    private Question markCallerState(Question question, String email) {
        markCallerState(List.of(question), email);
        return question;
    }

    // fills the transient flags the client needs, in one pass per page
    private List<Question> markCallerState(List<Question> questions, String email) {
        if (email == null || questions.isEmpty()) {
            return questions;
        }
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return questions;
        }

        Integer userId = user.get().getId();
        List<Integer> questionIds = questions.stream().map(Question::getId).toList();
        Set<Integer> upvotedIds = new HashSet<>(questionUpvoteRepository.findUpvotedQuestionIds(userId, questionIds));
        questions.forEach(question -> {
            question.setUpvoted(upvotedIds.contains(question.getId()));
            question.setOwned(question.getAskedBy() != null && Objects.equals(question.getAskedBy().getId(), userId));
        });
        return questions;
    }

    private Answer markOwned(Answer answer, String email) {
        markOwned(List.of(answer), email);
        return answer;
    }

    private List<Answer> markOwned(List<Answer> answers, String email) {
        if (email == null || answers.isEmpty()) {
            return answers;
        }
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return answers;
        }

        Integer userId = user.get().getId();
        answers.forEach(answer ->
                answer.setOwned(answer.getAnsweredBy() != null && Objects.equals(answer.getAnsweredBy().getId(), userId))
        );
        return answers;
    }

    // the slot is claimed in one statement, so simultaneous requests cannot both
    // pass, and it is kept on the user so deleting a post cannot reset it
    private void verifyQuestionCooldown(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cooldownStart = now.minusMinutes(Constants.QUESTION_COOLDOWN_MINUTES);
        if (userRepository.markQuestionAsked(user.getId(), now, cooldownStart) == 0) {
            throw new AcmException(
                    ErrorConstants.QUESTION_ASKED_TOO_SOON + Constants.QUESTION_COOLDOWN_MINUTES + " minutes",
                    HttpStatus.TOO_MANY_REQUESTS
            );
        }
    }

    private void verifyAnswerCooldown(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cooldownStart = now.minusMinutes(Constants.ANSWER_COOLDOWN_MINUTES);
        if (userRepository.markAnswerPosted(user.getId(), now, cooldownStart) == 0) {
            throw new AcmException(
                    ErrorConstants.ANSWER_POSTED_TOO_SOON + Constants.ANSWER_COOLDOWN_MINUTES + " minutes",
                    HttpStatus.TOO_MANY_REQUESTS
            );
        }
    }

    private void verifyAskedBy(Question question, String email) {
        User user = getVerifiedUser(email);
        if (question.getAskedBy() == null || !Objects.equals(question.getAskedBy().getId(), user.getId())) {
            throw new AcmException(ErrorConstants.QUESTION_NOT_OWNED, HttpStatus.FORBIDDEN);
        }
    }

    private void verifyAnsweredBy(Answer answer, String email) {
        User user = getVerifiedUser(email);
        if (answer.getAnsweredBy() == null || !Objects.equals(answer.getAnsweredBy().getId(), user.getId())) {
            throw new AcmException(ErrorConstants.ANSWER_NOT_OWNED, HttpStatus.FORBIDDEN);
        }
    }

    // login already blocks unverified users, but a token stays valid after it is issued
    private User getVerifiedUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new AcmException(ErrorConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND)
        );
        if (!Boolean.TRUE.equals(user.getVerified())) {
            throw new AcmException(ErrorConstants.USER_NOT_VERIFIED, HttpStatus.FORBIDDEN);
        }
        return user;
    }

    private void validateContent(String content) {
        if (Strings.isBlank(content)) {
            throw new AcmException(ErrorConstants.CONTENT_EMPTY, HttpStatus.BAD_REQUEST);
        }
    }
}
