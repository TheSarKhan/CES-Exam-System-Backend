package com.ces.exam.service;

import com.ces.exam.exception.ResourceNotFoundException;
import com.ces.exam.exception.ValidationException;
import com.ces.exam.model.entity.*;
import com.ces.exam.model.enums.ExamType;
import com.ces.exam.model.enums.QuestionType;
import com.ces.exam.model.enums.SessionStatus;
import com.ces.exam.payload.request.SessionAnswerRequest;
import com.ces.exam.payload.request.StartSessionRequest;
import com.ces.exam.payload.request.SubmitSessionRequest;
import com.ces.exam.payload.response.*;
import com.ces.exam.repository.*;
import com.ces.exam.security.UserDetailsImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamSessionService {

    private final ExamAssignmentRepository assignmentRepository;
    private final ExamSessionRepository sessionRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;

    public ExamSessionService(ExamAssignmentRepository assignmentRepository,
                              ExamSessionRepository sessionRepository,
                              QuestionRepository questionRepository,
                              QuestionOptionRepository questionOptionRepository) {
        this.assignmentRepository = assignmentRepository;
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
    }

    @Transactional
    public SessionStartResponse startSession(StartSessionRequest request) {
        User user = getCurrentUser();
        ExamAssignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        validateAssignmentAccess(assignment, user);
        validateAssignmentDates(assignment);
        return startSessionForAssignment(assignment, user);
    }

    @Transactional(readOnly = true)
    public TokenAssignmentResponse getAssignmentByToken(String token) {
        ExamAssignment assignment = resolveAssignmentByToken(token);
        User user = requireAssignedUser(assignment);
        Exam exam = assignment.getExam();

        String status = "PENDING";
        Long sessionId = null;
        Optional<ExamSession> existing = sessionRepository.findByAssignmentIdAndUserId(assignment.getId(), user.getId());
        if (existing.isPresent()) {
            sessionId = existing.get().getId();
            status = existing.get().getStatus() == SessionStatus.COMPLETED ? "COMPLETED" : "IN_PROGRESS";
        }

        return new TokenAssignmentResponse(
                exam.getTitle(),
                exam.getDescription(),
                user.getFirstName() + " " + user.getLastName(),
                exam.getDurationMinutes(),
                assignment.getStartDate(),
                assignment.getEndDate(),
                status,
                sessionId
        );
    }

    @Transactional
    public SessionStartResponse startSessionByToken(String token) {
        ExamAssignment assignment = resolveAssignmentByToken(token);
        validateAssignmentDates(assignment);
        User user = requireAssignedUser(assignment);
        return startSessionForAssignment(assignment, user);
    }

    @Transactional(readOnly = true)
    public SessionStartResponse getActiveSessionByToken(String token, Long sessionId) {
        ExamSession session = loadSessionForToken(token, sessionId);
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new ValidationException("Session is not active");
        }
        return mapToStartResponse(sessionRepository.findByIdWithDetails(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found")));
    }

    @Transactional
    public SessionResultResponse submitSessionByToken(String token, Long sessionId, SubmitSessionRequest request) {
        ExamSession session = loadSessionForToken(token, sessionId);
        return submitSessionInternal(session, request);
    }

    @Transactional(readOnly = true)
    public SessionResultResponse getSessionResultByToken(String token, Long sessionId) {
        ExamSession session = loadSessionForToken(token, sessionId);
        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new ValidationException("Session not yet completed");
        }
        return mapToResultResponse(sessionRepository.findByIdWithDetails(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found")));
    }

    private SessionStartResponse startSessionForAssignment(ExamAssignment assignment, User user) {
        Optional<ExamSession> existing = sessionRepository.findByAssignmentIdAndUserId(
                assignment.getId(), user.getId());

        if (existing.isPresent()) {
            ExamSession session = existing.get();
            if (session.getStatus() == SessionStatus.COMPLETED) {
                throw new ValidationException("Exam already completed");
            }
            ExamSession loaded = sessionRepository.findByIdWithDetails(session.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
            return mapToStartResponse(loaded);
        }

        Exam exam = assignment.getExam();
        List<Question> selectedQuestions = pickRandomQuestions(exam);

        ExamSession session = new ExamSession();
        session.setAssignment(assignment);
        session.setUser(user);
        session.setStatus(SessionStatus.IN_PROGRESS);

        java.util.Set<ExamSessionQuestion> sessionQuestions = selectedQuestions.stream().map(q -> {
            ExamSessionQuestion sq = new ExamSessionQuestion();
            sq.setQuestion(q);
            return sq;
        }).collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
        session.setSessionQuestions(sessionQuestions);

        ExamSession saved = sessionRepository.save(session);
        ExamSession loaded = sessionRepository.findByIdWithDetails(saved.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        return mapToStartResponse(loaded);
    }

    private ExamSession loadSessionForToken(String token, Long sessionId) {
        ExamSession session = sessionRepository.findByIdWithDetails(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        validateSessionBelongsToToken(session, token);
        return session;
    }

    private ExamAssignment resolveAssignmentByToken(String token) {
        return assignmentRepository.findByAccessToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired exam link"));
    }

    private User requireAssignedUser(ExamAssignment assignment) {
        if (assignment.getAssignedUser() == null) {
            throw new ValidationException("This exam link is not valid for individual access");
        }
        return assignment.getAssignedUser();
    }

    private void validateSessionBelongsToToken(ExamSession session, String token) {
        String assignmentToken = session.getAssignment().getAccessToken();
        if (assignmentToken == null || !assignmentToken.equals(token)) {
            throw new AccessDeniedException("Invalid access token");
        }
    }

    @Transactional
    public SessionResultResponse submitSession(Long sessionId, SubmitSessionRequest request) {
        User user = getCurrentUser();
        ExamSession session = sessionRepository.findByIdWithDetails(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }
        return submitSessionInternal(session, request);
    }

    private SessionResultResponse submitSessionInternal(ExamSession session, SubmitSessionRequest request) {
        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new ValidationException("Session already submitted");
        }

        Map<Long, SessionAnswerRequest> answerMap = request.getAnswers().stream()
                .collect(Collectors.toMap(SessionAnswerRequest::getQuestionId, a -> a, (a, b) -> b));

        BigDecimal earnedScore = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.ZERO;

        for (ExamSessionQuestion sq : session.getSessionQuestions()) {
            Question question = sq.getQuestion();
            maxScore = maxScore.add(question.getScore() != null ? question.getScore() : BigDecimal.ONE);

            SessionAnswerRequest answer = answerMap.get(question.getId());
            if (answer == null) continue;

            if (answer.getSelectedOptionId() != null) {
                QuestionOption option = questionOptionRepository.findById(answer.getSelectedOptionId())
                        .orElseThrow(() -> new ResourceNotFoundException("Option not found"));
                sq.setSelectedOption(option);
            }
            if (answer.getSelectedOptionIds() != null && !answer.getSelectedOptionIds().isEmpty()) {
                Set<QuestionOption> options = new HashSet<>(questionOptionRepository.findAllById(answer.getSelectedOptionIds()));
                sq.setSelectedOptions(options);
            }
            if (answer.getTextAnswer() != null) {
                sq.setTextAnswer(answer.getTextAnswer());
            }

            Boolean correct = evaluateAnswer(question, sq);
            sq.setIsCorrect(correct);
            if (Boolean.TRUE.equals(correct)) {
                earnedScore = earnedScore.add(question.getScore() != null ? question.getScore() : BigDecimal.ONE);
            }
        }

        Exam exam = session.getAssignment().getExam();
        BigDecimal percentageScore = maxScore.compareTo(BigDecimal.ZERO) > 0
                ? earnedScore.multiply(BigDecimal.valueOf(100)).divide(maxScore, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Boolean passed = null;
        if (exam.getType() == ExamType.EXAM && exam.getPassMark() != null) {
            passed = percentageScore.compareTo(exam.getPassMark()) >= 0;
        }

        session.setScore(percentageScore);
        session.setPassed(passed);
        session.setStatus(SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());
        sessionRepository.save(session);

        return mapToResultResponse(session);
    }

    @Transactional(readOnly = true)
    public SessionStartResponse getActiveSession(Long sessionId) {
        User user = getCurrentUser();
        ExamSession session = sessionRepository.findByIdWithDetails(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new ValidationException("Session is not active");
        }

        return mapToStartResponse(session);
    }

    @Transactional(readOnly = true)
    public SessionResultResponse getSessionResult(Long sessionId) {
        User user = getCurrentUser();
        ExamSession session = sessionRepository.findByIdWithDetails(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }
        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new ValidationException("Session not yet completed");
        }

        return mapToResultResponse(session);
    }

    @Transactional(readOnly = true)
    public List<MyAssignmentResponse> getMyAssignments() {
        User user = getCurrentUser();
        Long departmentId = user.getDepartment() != null ? user.getDepartment().getId() : null;

        List<ExamAssignment> assignments = assignmentRepository.findMyAssignments(user.getId(), departmentId);
        if (assignments.isEmpty()) return List.of();

        List<Long> assignmentIds = assignments.stream().map(ExamAssignment::getId).collect(Collectors.toList());
        List<ExamSession> sessions = sessionRepository.findByUserIdAndAssignmentIdIn(user.getId(), assignmentIds);
        Map<Long, ExamSession> sessionByAssignment = sessions.stream()
                .collect(Collectors.toMap(s -> s.getAssignment().getId(), s -> s, (a, b) ->
                        a.getStartTime().isAfter(b.getStartTime()) ? a : b));

        return assignments.stream().map(a -> {
            ExamSession session = sessionByAssignment.get(a.getId());
            String status = "PENDING";
            Long sessionId = null;
            BigDecimal score = null;
            Boolean passed = null;

            if (session != null) {
                sessionId = session.getId();
                if (session.getStatus() == SessionStatus.COMPLETED) {
                    status = "COMPLETED";
                    score = session.getScore();
                    passed = session.getPassed();
                } else {
                    status = "IN_PROGRESS";
                }
            }

            Exam exam = a.getExam();
            return new MyAssignmentResponse(
                    a.getId(), exam.getId(), exam.getTitle(), exam.getType().name(),
                    exam.getDurationMinutes(), a.getStartDate(), a.getEndDate(),
                    status, sessionId, score, passed
            );
        }).collect(Collectors.toList());
    }

    private List<Question> pickRandomQuestions(Exam exam) {
        List<Question> result = new ArrayList<>();
        if (exam.getTopicConfigs() == null || exam.getTopicConfigs().isEmpty()) {
            throw new ValidationException("Exam has no topic configurations");
        }

        for (ExamTopicConfig config : exam.getTopicConfigs()) {
            List<Question> pool = questionRepository.findByTopicIdAndIsActiveTrue(config.getTopic().getId());
            if (pool.size() < config.getQuestionCount()) {
                throw new ValidationException("Not enough questions in topic: " + config.getTopic().getName());
            }
            Collections.shuffle(pool);
            result.addAll(pool.subList(0, config.getQuestionCount()));
        }
        Collections.shuffle(result);
        return result;
    }

    private void validateAssignmentAccess(ExamAssignment assignment, User user) {
        if (assignment.getAssignedUser() != null &&
                assignment.getAssignedUser().getId().equals(user.getId())) {
            return;
        }
        if (assignment.getAssignedDepartment() != null && user.getDepartment() != null &&
                assignment.getAssignedDepartment().getId().equals(user.getDepartment().getId())) {
            return;
        }
        throw new ValidationException("Assignment not accessible for this user");
    }

    private void validateAssignmentDates(ExamAssignment assignment) {
        LocalDateTime now = LocalDateTime.now();
        if (assignment.getStartDate() != null && now.isBefore(assignment.getStartDate())) {
            throw new ValidationException("Exam has not started yet");
        }
        if (assignment.getEndDate() != null && now.isAfter(assignment.getEndDate())) {
            throw new ValidationException("Exam deadline has passed");
        }
    }

    private Boolean evaluateAnswer(Question question, ExamSessionQuestion sq) {
        QuestionType type = question.getType();
        if (type == QuestionType.MULTIPLE_CHOICE) {
            if (sq.getSelectedOptions() == null || sq.getSelectedOptions().isEmpty()) return false;
            Set<Long> correctIds = question.getOptions().stream()
                    .filter(o -> Boolean.TRUE.equals(o.getIsCorrect()))
                    .map(QuestionOption::getId)
                    .collect(Collectors.toSet());
            Set<Long> selectedIds = sq.getSelectedOptions().stream()
                    .map(QuestionOption::getId)
                    .collect(Collectors.toSet());
            return correctIds.equals(selectedIds);
        }
        if (type == QuestionType.SINGLE_CHOICE || type == QuestionType.TRUE_FALSE ||
                type == QuestionType.IMAGE_CHOICE) {
            if (sq.getSelectedOption() == null) return false;
            return Boolean.TRUE.equals(sq.getSelectedOption().getIsCorrect());
        }
        return null;
    }

    private SessionStartResponse mapToStartResponse(ExamSession session) {
        Exam exam = session.getAssignment().getExam();
        List<SessionQuestionResponse> questions = session.getSessionQuestions().stream()
                .map(sq -> mapQuestionForSession(sq.getQuestion()))
                .collect(Collectors.toList());

        return new SessionStartResponse(
                session.getId(),
                session.getAssignment().getId(),
                exam.getTitle(),
                exam.getDurationMinutes(),
                session.getStartTime(),
                questions
        );
    }

    private SessionQuestionResponse mapQuestionForSession(Question question) {
        List<SessionQuestionOptionResponse> options = null;
        if (question.getOptions() != null) {
            options = question.getOptions().stream()
                    .map(o -> new SessionQuestionOptionResponse(o.getId(), o.getText(), o.getSortOrder()))
                    .collect(Collectors.toList());
        }
        return new SessionQuestionResponse(
                question.getId(),
                question.getType().name(),
                question.getText(),
                question.getScore(),
                options
        );
    }

    private SessionResultResponse mapToResultResponse(ExamSession session) {
        Exam exam = session.getAssignment().getExam();
        List<SessionAnswerResultResponse> answers = session.getSessionQuestions().stream()
                .map(sq -> {
                    Question q = sq.getQuestion();
                    String optionText = sq.getSelectedOption() != null ? sq.getSelectedOption().getText() : null;
                    Long optionId = sq.getSelectedOption() != null ? sq.getSelectedOption().getId() : null;
                    return new SessionAnswerResultResponse(
                            q.getId(), q.getText(), q.getType().name(),
                            optionId, optionText, sq.getTextAnswer(),
                            sq.getIsCorrect(), q.getScore()
                    );
                }).collect(Collectors.toList());

        return new SessionResultResponse(
                session.getId(),
                exam.getTitle(),
                session.getStatus().name(),
                session.getScore(),
                session.getPassed(),
                exam.getPassMark(),
                session.getStartTime(),
                session.getEndTime(),
                answers
        );
    }

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userDetails.getUser();
    }
}
