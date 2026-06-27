package com.ces.exam.service;

import com.ces.exam.exception.ResourceNotFoundException;
import com.ces.exam.exception.ValidationException;
import com.ces.exam.model.entity.*;
import com.ces.exam.model.enums.ExamType;
import com.ces.exam.model.enums.QuestionType;
import com.ces.exam.model.enums.SessionStatus;
import com.ces.exam.payload.request.GradeSessionRequest;
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
    private final SessionViolationRepository violationRepository;
    private final CandidateService candidateService;
    private final SettingsService settingsService;

    public ExamSessionService(ExamAssignmentRepository assignmentRepository,
                              ExamSessionRepository sessionRepository,
                              QuestionRepository questionRepository,
                              QuestionOptionRepository questionOptionRepository,
                              SessionViolationRepository violationRepository,
                              CandidateService candidateService,
                              SettingsService settingsService) {
        this.assignmentRepository = assignmentRepository;
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.violationRepository = violationRepository;
        this.candidateService = candidateService;
        this.settingsService = settingsService;
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
        Exam exam = assignment.getExam();
        User user = assignment.getAssignedUser(); // null for an anonymous link not yet started

        String status = "PENDING";
        Long sessionId = null;
        String candidateName = null;
        if (user != null) {
            candidateName = (user.getFirstName() + " " + user.getLastName()).trim();
            Optional<ExamSession> existing = sessionRepository.findByAssignmentIdAndUserId(assignment.getId(), user.getId());
            if (existing.isPresent()) {
                sessionId = existing.get().getId();
                status = existing.get().getStatus() == SessionStatus.COMPLETED ? "COMPLETED" : "IN_PROGRESS";
            }
        }

        return new TokenAssignmentResponse(
                exam.getTitle(),
                exam.getDescription(),
                candidateName,
                exam.getDurationMinutes(),
                assignment.getStartDate(),
                assignment.getEndDate(),
                status,
                sessionId
        );
    }

    @Transactional
    public SessionStartResponse startSessionByToken(String token, String candidateName) {
        ExamAssignment assignment = resolveAssignmentByToken(token);
        validateAssignmentDates(assignment);

        // Single-use, race-safe: only the request that flips consumed false→true wins.
        if (assignmentRepository.markConsumed(assignment.getId()) == 0) {
            throw new ValidationException("Bu link artıq istifadə olunub.");
        }
        assignment.setConsumed(true); // keep the in-memory entity consistent with the DB

        User user = assignment.getAssignedUser();
        if (user == null) {
            // Anonymous link → the taker names themselves now; create their account.
            user = candidateService.create(candidateName);
            assignment.setAssignedUser(user);
        }
        assignmentRepository.save(assignment);

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
        return maskIfHidden(submitSessionInternal(session, request));
    }

    @Transactional(readOnly = true)
    public SessionResultResponse getSessionResultByToken(String token, Long sessionId) {
        ExamSession session = loadSessionForToken(token, sessionId);
        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new ValidationException("Session not yet completed");
        }
        return maskIfHidden(mapToResultResponse(sessionRepository.findByIdWithDetails(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"))));
    }

    /** Candidate (token) results are hidden when the admin disabled "show result to candidate". */
    private SessionResultResponse maskIfHidden(SessionResultResponse full) {
        if (settingsService.isShowResultToCandidate()) return full;
        return SessionResultResponse.hidden(full.getSessionId(), full.getExamTitle(), full.getStatus(),
                full.getStartTime(), full.getEndTime(), full.getTerminationReason());
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
        List<Question> selectedQuestions = selectQuestions(exam);
        if (settingsService.isShuffleQuestions()) {
            Collections.shuffle(selectedQuestions);
        }

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

        // Server-side time-limit guard: the client timer can be bypassed, so flag a
        // submission that arrives past the exam's duration window (plus a small grace).
        boolean lateSubmission = isPastDuration(session);

        Map<Long, SessionAnswerRequest> answerMap = request.getAnswers().stream()
                .collect(Collectors.toMap(SessionAnswerRequest::getQuestionId, a -> a, (a, b) -> b));

        // Manual grading only applies to real exams; surveys are never scored/graded.
        boolean gradable = session.getAssignment().getExam().getType() == ExamType.EXAM;

        for (ExamSessionQuestion sq : session.getSessionQuestions()) {
            Question question = sq.getQuestion();
            SessionAnswerRequest answer = answerMap.get(question.getId());

            if (answer != null) {
                List<QuestionOption> validOptions = question.getOptions() != null
                        ? question.getOptions() : List.of();
                if (answer.getSelectedOptionId() != null) {
                    // Only accept an option that actually belongs to THIS question — prevents
                    // submitting a foreign option id to tamper with grading.
                    QuestionOption option = validOptions.stream()
                            .filter(o -> o.getId().equals(answer.getSelectedOptionId()))
                            .findFirst()
                            .orElseThrow(() -> new ValidationException("Seçilmiş variant bu suala aid deyil"));
                    sq.setSelectedOption(option);
                }
                if (answer.getSelectedOptionIds() != null && !answer.getSelectedOptionIds().isEmpty()) {
                    Set<Long> requested = new HashSet<>(answer.getSelectedOptionIds());
                    Set<QuestionOption> options = validOptions.stream()
                            .filter(o -> requested.contains(o.getId()))
                            .collect(Collectors.toSet());
                    if (options.size() != requested.size()) {
                        throw new ValidationException("Seçilmiş variantlardan biri bu suala aid deyil");
                    }
                    sq.setSelectedOptions(options);
                }
                if (answer.getTextAnswer() != null) {
                    String text = answer.getTextAnswer();
                    if (text.length() > MAX_TEXT_ANSWER_LENGTH) {
                        text = text.substring(0, MAX_TEXT_ANSWER_LENGTH);
                    }
                    sq.setTextAnswer(text);
                }
            }

            BigDecimal qScore = question.getScore() != null ? question.getScore() : BigDecimal.ONE;
            Boolean correct = evaluateAnswer(question, sq);
            if (correct == null) {
                boolean blank = sq.getTextAnswer() == null || sq.getTextAnswer().isBlank();
                if (gradable && !blank) {
                    sq.setIsCorrect(null);            // open-ended exam answer → awaits manual grading
                    sq.setAwardedScore(null);
                } else if (gradable) {
                    sq.setIsCorrect(false);           // blank exam answer → zero, nothing to grade
                    sq.setAwardedScore(BigDecimal.ZERO);
                } else {
                    sq.setIsCorrect(null);            // survey response → not scored, not graded
                    sq.setAwardedScore(null);
                }
            } else {
                sq.setIsCorrect(correct);
                sq.setAwardedScore(Boolean.TRUE.equals(correct) ? qScore : BigDecimal.ZERO);
            }
        }

        recomputeScore(session);
        session.setStatus(SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());
        // A CRITICAL violation is only ever emitted when the anti-cheat limit is reached,
        // i.e. the exam was auto-terminated rather than submitted by the candidate.
        if (wasProctoringTerminated(request.getViolations())) {
            session.setTerminationReason("PROCTORING");
        } else if (lateSubmission) {
            session.setTerminationReason("TIMEOUT");
        }
        sessionRepository.save(session);

        saveViolations(session, request.getViolations());

        return mapToResultResponse(session);
    }

    /** True when the submitted violations include the CRITICAL strike that the anti-cheat limit triggers. */
    private boolean wasProctoringTerminated(List<SubmitSessionRequest.ViolationRequest> violations) {
        if (violations == null) return false;
        return violations.stream()
                .anyMatch(v -> v != null && "CRITICAL".equalsIgnoreCase(v.getSeverity()));
    }

    private void saveViolations(ExamSession session, List<SubmitSessionRequest.ViolationRequest> violations) {
        if (violations == null || violations.isEmpty()) return;
        List<SessionViolation> entities = new ArrayList<>();
        for (SubmitSessionRequest.ViolationRequest v : violations) {
            if (v.getType() == null) continue;
            SessionViolation sv = new SessionViolation();
            sv.setSession(session);
            sv.setType(v.getType());
            sv.setLabel(v.getLabel());
            sv.setSeverity(v.getSeverity() != null ? v.getSeverity() : "LOGGED");
            LocalDateTime when = v.getAt() != null
                    ? LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(v.getAt()), java.time.ZoneId.systemDefault())
                    : LocalDateTime.now();
            sv.setOccurredAt(when);
            entities.add(sv);
        }
        if (!entities.isEmpty()) violationRepository.saveAll(entities);
    }

    @Transactional(readOnly = true)
    public List<ViolationResponse> getSessionViolations(Long sessionId) {
        return violationRepository.findBySessionIdOrderByOccurredAtAsc(sessionId).stream()
                .map(v -> new ViolationResponse(v.getType(), v.getLabel(), v.getSeverity(), v.getOccurredAt()))
                .collect(Collectors.toList());
    }

    /**
     * Recomputes a session's percentage score and pass/fail from per-answer awarded
     * points. While any answer still awaits manual grading the pass/fail verdict is
     * left null (we can't decide yet); the score reflects what's graded so far.
     */
    private void recomputeScore(ExamSession session) {
        BigDecimal earned = BigDecimal.ZERO;
        BigDecimal max = BigDecimal.ZERO;
        int pending = 0;

        for (ExamSessionQuestion sq : session.getSessionQuestions()) {
            BigDecimal qScore = sq.getQuestion().getScore() != null ? sq.getQuestion().getScore() : BigDecimal.ONE;
            max = max.add(qScore);
            if (sq.getIsCorrect() == null) {
                pending++;
            } else if (sq.getAwardedScore() != null) {
                earned = earned.add(sq.getAwardedScore());
            } else if (Boolean.TRUE.equals(sq.getIsCorrect())) {
                earned = earned.add(qScore);
            }
        }

        BigDecimal percentageScore = max.compareTo(BigDecimal.ZERO) > 0
                ? earned.multiply(BigDecimal.valueOf(100)).divide(max, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Exam exam = session.getAssignment().getExam();
        Boolean passed = null;
        if (pending == 0 && exam.getType() == ExamType.EXAM && exam.getPassMark() != null) {
            passed = percentageScore.compareTo(exam.getPassMark()) >= 0;
        }

        session.setScore(percentageScore);
        session.setPassed(passed);
    }

    @Transactional
    public SessionResultResponse gradeSession(Long sessionId, GradeSessionRequest request) {
        ExamSession session = sessionRepository.findByIdWithDetails(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new ValidationException("Yalnız tamamlanmış sessiyalar qiymətləndirilə bilər.");
        }

        Map<Long, BigDecimal> gradeMap = new HashMap<>();
        if (request.getGrades() != null) {
            for (GradeSessionRequest.AnswerGrade g : request.getGrades()) {
                if (g.getQuestionId() != null && g.getAwardedScore() != null) {
                    gradeMap.put(g.getQuestionId(), g.getAwardedScore());
                }
            }
        }

        for (ExamSessionQuestion sq : session.getSessionQuestions()) {
            // Only answers awaiting manual grading can be (re)graded here.
            if (sq.getIsCorrect() != null) continue;
            BigDecimal awarded = gradeMap.get(sq.getQuestion().getId());
            if (awarded == null) continue;

            BigDecimal qScore = sq.getQuestion().getScore() != null ? sq.getQuestion().getScore() : BigDecimal.ONE;
            if (awarded.compareTo(BigDecimal.ZERO) < 0) awarded = BigDecimal.ZERO;
            if (awarded.compareTo(qScore) > 0) awarded = qScore;

            sq.setAwardedScore(awarded);
            // Full marks counts as "correct"; partial/zero keeps its points but isn't "correct".
            sq.setIsCorrect(awarded.compareTo(qScore) >= 0);
        }

        recomputeScore(session);
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
    public SessionResultResponse getSessionResultForAdmin(Long sessionId) {
        ExamSession session = sessionRepository.findByIdWithDetails(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        return mapToResultResponse(session);
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

    private List<Question> selectQuestions(Exam exam) {
        // A concrete, ordered question list takes precedence (built in the exam builder).
        if (exam.getExamQuestions() != null && !exam.getExamQuestions().isEmpty()) {
            return exam.getExamQuestions().stream()
                    .sorted(Comparator.comparing(eq -> eq.getSortOrder() != null ? eq.getSortOrder() : 0))
                    .map(ExamQuestion::getQuestion)
                    .collect(Collectors.toList());
        }
        return pickRandomQuestions(exam);
    }

    private List<Question> pickRandomQuestions(Exam exam) {
        List<Question> result = new ArrayList<>();
        if (exam.getTopicConfigs() == null || exam.getTopicConfigs().isEmpty()) {
            throw new ValidationException("Exam has no questions");
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

    /** Largest text answer we persist; guards against multi-megabyte answer DoS. */
    private static final int MAX_TEXT_ANSWER_LENGTH = 50_000;

    /** Grace window (seconds) added to the duration to absorb clock skew / network lag. */
    private static final long SUBMIT_GRACE_SECONDS = 30;

    /** True when this submission arrives after the exam's allowed duration window. */
    private boolean isPastDuration(ExamSession session) {
        Integer minutes = session.getAssignment().getExam().getDurationMinutes();
        if (minutes == null || minutes <= 0 || session.getStartTime() == null) {
            return false; // untimed exam → nothing to enforce
        }
        LocalDateTime deadline = session.getStartTime().plusMinutes(minutes).plusSeconds(SUBMIT_GRACE_SECONDS);
        return LocalDateTime.now().isAfter(deadline);
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
        boolean shuffleOptions = settingsService.isShuffleOptions();
        List<SessionQuestionResponse> questions = session.getSessionQuestions().stream()
                .map(sq -> mapQuestionForSession(sq.getQuestion(), session.getId(), shuffleOptions))
                .collect(Collectors.toList());

        return new SessionStartResponse(
                session.getId(),
                session.getAssignment().getId(),
                exam.getTitle(),
                exam.getDurationMinutes(),
                session.getStartTime(),
                LocalDateTime.now(),
                questions
        );
    }

    private SessionQuestionResponse mapQuestionForSession(Question question, Long sessionId, boolean shuffleOptions) {
        List<SessionQuestionOptionResponse> options = null;
        if (question.getOptions() != null) {
            List<QuestionOption> ordered = new ArrayList<>(question.getOptions());
            if (shuffleOptions) {
                // Deterministic per (session, question, option) so the order is stable across
                // reloads of the same session, but randomized differently for each candidate.
                ordered.sort(Comparator.comparingInt(o -> Objects.hash(sessionId, question.getId(), o.getId())));
            }
            options = ordered.stream()
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
        boolean isExamType = exam.getType() == ExamType.EXAM;

        List<SessionAnswerResultResponse> answers = new ArrayList<>();
        BigDecimal earned = BigDecimal.ZERO;
        BigDecimal max = BigDecimal.ZERO;
        int pending = 0;

        for (ExamSessionQuestion sq : session.getSessionQuestions()) {
            Question q = sq.getQuestion();
            BigDecimal qScore = q.getScore() != null ? q.getScore() : BigDecimal.ONE;
            max = max.add(qScore);

            boolean needsGrading = isExamType && sq.getIsCorrect() == null;
            if (needsGrading) {
                pending++;
            } else if (sq.getAwardedScore() != null) {
                earned = earned.add(sq.getAwardedScore());
            } else if (Boolean.TRUE.equals(sq.getIsCorrect())) {
                earned = earned.add(qScore);
            }

            String optionText;
            Long optionId = null;
            if (sq.getSelectedOption() != null) {
                optionText = sq.getSelectedOption().getText();
                optionId = sq.getSelectedOption().getId();
            } else if (sq.getSelectedOptions() != null && !sq.getSelectedOptions().isEmpty()) {
                // Multiple-choice: join all chosen options.
                optionText = sq.getSelectedOptions().stream()
                        .map(QuestionOption::getText)
                        .collect(Collectors.joining(", "));
            } else {
                optionText = null;
            }

            answers.add(new SessionAnswerResultResponse(
                    q.getId(), q.getText(), q.getType().name(),
                    optionId, optionText, sq.getTextAnswer(),
                    sq.getIsCorrect(), qScore, sq.getAwardedScore(), needsGrading
            ));
        }

        return new SessionResultResponse(
                session.getId(),
                exam.getTitle(),
                session.getStatus().name(),
                session.getScore(),
                session.getPassed(),
                exam.getPassMark(),
                session.getStartTime(),
                session.getEndTime(),
                answers,
                pending,
                earned,
                max,
                session.getTerminationReason()
        );
    }

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userDetails.getUser();
    }
}
