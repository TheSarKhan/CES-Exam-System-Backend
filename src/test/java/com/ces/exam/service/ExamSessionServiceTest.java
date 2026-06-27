package com.ces.exam.service;

import com.ces.exam.exception.ValidationException;
import com.ces.exam.model.entity.*;
import com.ces.exam.model.enums.ExamType;
import com.ces.exam.model.enums.QuestionType;
import com.ces.exam.model.enums.SessionStatus;
import com.ces.exam.payload.request.SessionAnswerRequest;
import com.ces.exam.payload.request.SubmitSessionRequest;
import com.ces.exam.payload.response.SessionResultResponse;
import com.ces.exam.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the exam-submission critical path: answer-option validation and
 * grading. Pure Mockito — no Spring context / DB needed.
 */
class ExamSessionServiceTest {

    private ExamSessionRepository sessionRepository;
    private SettingsService settingsService;
    private ExamSessionService service;

    private ExamSession session;
    private QuestionOption correct;
    private QuestionOption wrong;
    private Question question;

    @BeforeEach
    void setup() {
        ExamAssignmentRepository assignmentRepository = mock(ExamAssignmentRepository.class);
        sessionRepository = mock(ExamSessionRepository.class);
        QuestionRepository questionRepository = mock(QuestionRepository.class);
        QuestionOptionRepository questionOptionRepository = mock(QuestionOptionRepository.class);
        SessionViolationRepository violationRepository = mock(SessionViolationRepository.class);
        CandidateService candidateService = mock(CandidateService.class);
        settingsService = mock(SettingsService.class);
        when(settingsService.isShowResultToCandidate()).thenReturn(true);

        service = new ExamSessionService(assignmentRepository, sessionRepository, questionRepository,
                questionOptionRepository, violationRepository, candidateService, settingsService);

        Exam exam = new Exam();
        exam.setType(ExamType.EXAM);
        exam.setTitle("Test exam");
        exam.setDurationMinutes(30);

        correct = new QuestionOption();
        correct.setId(10L);
        correct.setText("Right");
        correct.setIsCorrect(true);
        wrong = new QuestionOption();
        wrong.setId(11L);
        wrong.setText("Wrong");
        wrong.setIsCorrect(false);

        question = new Question();
        question.setId(1L);
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setText("Q1");
        question.setScore(BigDecimal.ONE);
        question.setOptions(List.of(correct, wrong));

        ExamAssignment assignment = new ExamAssignment();
        assignment.setAccessToken("tok");
        assignment.setExam(exam);

        ExamSessionQuestion sq = new ExamSessionQuestion();
        sq.setQuestion(question);

        session = new ExamSession();
        session.setStatus(SessionStatus.IN_PROGRESS);
        session.setStartTime(LocalDateTime.now());
        session.setAssignment(assignment);
        session.setSessionQuestions(new java.util.LinkedHashSet<>(Set.of(sq)));

        when(sessionRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(session));
    }

    private SubmitSessionRequest answer(Long optionId) {
        SessionAnswerRequest a = new SessionAnswerRequest();
        a.setQuestionId(1L);
        a.setSelectedOptionId(optionId);
        SubmitSessionRequest req = new SubmitSessionRequest();
        req.setAnswers(List.of(a));
        return req;
    }

    @Test
    void submittingTheCorrectOption_gradesItAndCompletesTheSession() {
        SessionResultResponse result = service.submitSessionByToken("tok", 1L, answer(correct.getId()));

        assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(result).isNotNull();
        ExamSessionQuestion graded = session.getSessionQuestions().iterator().next();
        assertThat(graded.getIsCorrect()).isTrue();
        assertThat(graded.getAwardedScore()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void submittingAWrongOption_scoresZero() {
        service.submitSessionByToken("tok", 1L, answer(wrong.getId()));

        ExamSessionQuestion graded = session.getSessionQuestions().iterator().next();
        assertThat(graded.getIsCorrect()).isFalse();
        assertThat(graded.getAwardedScore()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void submittingAnOptionFromAnotherQuestion_isRejected() {
        // 999 does not belong to this question's options → tamper attempt must fail.
        assertThatThrownBy(() -> service.submitSessionByToken("tok", 1L, answer(999L)))
                .isInstanceOf(ValidationException.class);
        assertThat(session.getStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
    }
}
