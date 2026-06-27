package com.ces.exam.service;

import com.ces.exam.model.entity.*;
import com.ces.exam.model.enums.Difficulty;
import com.ces.exam.model.enums.ExamType;
import com.ces.exam.model.enums.SessionStatus;
import com.ces.exam.payload.request.ExamAssignmentRequest;
import com.ces.exam.payload.request.ExamRequest;
import com.ces.exam.payload.response.ExamAssignmentResponse;
import com.ces.exam.payload.response.ExamResponse;
import com.ces.exam.payload.response.QuestionOptionResponse;
import com.ces.exam.repository.*;
import com.ces.exam.exception.ResourceNotFoundException;
import com.ces.exam.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExamService {
    private final ExamRepository examRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ExamAssignmentRepository examAssignmentRepository;
    private final QuestionRepository questionRepository;
    private final ExamSessionRepository examSessionRepository;
    private final ExamSessionQuestionRepository examSessionQuestionRepository;
    private final SessionViolationRepository sessionViolationRepository;
    private final CandidateService candidateService;
    private final EmailService emailService;

    public ExamService(ExamRepository examRepository, TopicRepository topicRepository, UserRepository userRepository,
                       DepartmentRepository departmentRepository, ExamAssignmentRepository examAssignmentRepository,
                       QuestionRepository questionRepository, ExamSessionRepository examSessionRepository,
                       ExamSessionQuestionRepository examSessionQuestionRepository,
                       SessionViolationRepository sessionViolationRepository,
                       CandidateService candidateService,
                       EmailService emailService) {
        this.examRepository = examRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.examAssignmentRepository = examAssignmentRepository;
        this.questionRepository = questionRepository;
        this.examSessionRepository = examSessionRepository;
        this.examSessionQuestionRepository = examSessionQuestionRepository;
        this.sessionViolationRepository = sessionViolationRepository;
        this.candidateService = candidateService;
        this.emailService = emailService;
    }

    @Transactional
    public ExamResponse createExam(ExamRequest request) {
        Exam exam = new Exam();
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setType(request.getType());
        exam.setPassMark(request.getPassMark());
        exam.setDurationMinutes(request.getDurationMinutes());

        if (request.getTopicConfigs() != null && !request.getTopicConfigs().isEmpty()) {
            List<ExamTopicConfig> configs = request.getTopicConfigs().stream().map(req -> {
                Topic topic = topicRepository.findById(req.getTopicId())
                        .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
                ExamTopicConfig config = new ExamTopicConfig();
                config.setTopic(topic);
                config.setQuestionCount(req.getQuestionCount());
                return config;
            }).collect(Collectors.toList());
            exam.setTopicConfigs(configs);
        }

        Exam saved = examRepository.save(exam);

        // Concrete question list: bank references + inline-authored questions.
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            List<ExamQuestion> examQuestions = new ArrayList<>();
            int order = 0;
            for (ExamRequest.ExamQuestionRequest item : request.getQuestions()) {
                Question question;
                if (item.getQuestionId() != null) {
                    question = questionRepository.findById(item.getQuestionId())
                            .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
                } else {
                    question = questionRepository.save(buildInlineQuestion(item, saved));
                }
                ExamQuestion eq = new ExamQuestion();
                eq.setQuestion(question);
                eq.setSortOrder(order++);
                examQuestions.add(eq);
            }
            saved.setExamQuestions(examQuestions);
            saved = examRepository.save(saved);
        }

        return mapToResponse(saved);
    }

    private Question buildInlineQuestion(ExamRequest.ExamQuestionRequest item, Exam owner) {
        Question q = new Question();
        q.setOwnerExam(owner);
        q.setTopic(null);
        q.setType(item.getType());
        q.setText(item.getText());
        q.setScore(item.getScore());
        q.setDifficulty(item.getDifficulty() != null ? item.getDifficulty() : Difficulty.MEDIUM);
        if (item.getOptions() != null) {
            List<QuestionOption> options = item.getOptions().stream().map(optReq -> {
                QuestionOption opt = new QuestionOption();
                opt.setText(optReq.getText());
                opt.setCorrect(optReq.getIsCorrect());
                opt.setSortOrder(optReq.getSortOrder());
                return opt;
            }).collect(Collectors.toList());
            q.setOptions(options);
        }
        return q;
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> getAllExams() {
        return examRepository.findAll().stream()
                .map(e -> attachStats(mapToResponse(e), e))
                .collect(Collectors.toList());
    }

    /** Paginated variant (opt-in via ?page=). */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ExamResponse> getAllExams(org.springframework.data.domain.Pageable pageable) {
        return examRepository.findAll(pageable).map(e -> attachStats(mapToResponse(e), e));
    }

    @Transactional(readOnly = true)
    public ExamResponse getExam(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
        return attachStats(mapToResponse(exam), exam);
    }

    private ExamResponse attachStats(ExamResponse response, Exam exam) {
        Long examId = exam.getId();
        int assigned = (int) examAssignmentRepository.countByExamId(examId);
        int completed = (int) examSessionRepository.countByAssignment_Exam_IdAndStatus(examId, SessionStatus.COMPLETED);
        int inProgress = (int) examSessionRepository.countByAssignment_Exam_IdAndStatus(examId, SessionStatus.IN_PROGRESS);

        Integer avgScore = null;
        Integer passRate = null;
        if (exam.getType() == ExamType.EXAM && completed > 0) {
            Double avg = examSessionRepository.avgScoreForExam(examId);
            if (avg != null) avgScore = (int) Math.round(avg);
            long passed = examSessionRepository.countPassedForExam(examId);
            passRate = (int) Math.round(passed * 100.0 / completed);
        }

        response.setStats(new ExamResponse.ExamStats(assigned, completed, inProgress, avgScore, passRate));
        return response;
    }

    @Transactional
    public ExamResponse updateExam(Long id, ExamRequest request) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setType(request.getType());
        exam.setPassMark(request.getPassMark());
        exam.setDurationMinutes(request.getDurationMinutes());

        // Editing through the builder makes the exam a fixed question list.
        if (exam.getTopicConfigs() != null) {
            exam.getTopicConfigs().clear();
        }

        List<ExamQuestion> rebuilt = new ArrayList<>();
        if (request.getQuestions() != null) {
            int order = 0;
            for (ExamRequest.ExamQuestionRequest item : request.getQuestions()) {
                Question question;
                if (item.getQuestionId() != null) {
                    question = questionRepository.findById(item.getQuestionId())
                            .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
                } else {
                    question = questionRepository.save(buildInlineQuestion(item, exam));
                }
                ExamQuestion eq = new ExamQuestion();
                eq.setExam(exam);
                eq.setQuestion(question);
                eq.setSortOrder(order++);
                rebuilt.add(eq);
            }
        }
        if (exam.getExamQuestions() == null) {
            exam.setExamQuestions(rebuilt);
        } else {
            exam.getExamQuestions().clear();
            exam.getExamQuestions().addAll(rebuilt);
        }

        return mapToResponse(examRepository.save(exam));
    }

    @Transactional(readOnly = true)
    public com.ces.exam.payload.response.ExamResultsResponse getExamResults(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        List<com.ces.exam.payload.response.ExamResultsResponse.SessionRow> sessions =
                examSessionRepository.findByExamIdWithUser(id).stream()
                        .map(s -> new com.ces.exam.payload.response.ExamResultsResponse.SessionRow(
                                s.getId(),
                                (s.getUser().getFirstName() + " " + s.getUser().getLastName()).trim(),
                                s.getStatus().name(),
                                s.getScore(),
                                s.getPassed(),
                                s.getStartTime(),
                                s.getEndTime(),
                                s.getStatus() == SessionStatus.COMPLETED
                                        ? (int) examSessionQuestionRepository.countPendingGradingForSession(s.getId())
                                        : 0,
                                (int) sessionViolationRepository.countBySessionId(s.getId())))
                        .collect(Collectors.toList());

        // Hide unused links to recipients who already used a link for this exam — they're redundant.
        java.util.Set<String> usedEmails = new java.util.HashSet<>(
                examAssignmentRepository.findUsedRecipientEmails(id));
        List<com.ces.exam.payload.response.ExamResultsResponse.PendingLink> pendingLinks =
                examAssignmentRepository.findPendingLinks(id).stream()
                        .filter(a -> a.getRecipientEmail() == null
                                || !usedEmails.contains(a.getRecipientEmail().toLowerCase()))
                        .map(a -> new com.ces.exam.payload.response.ExamResultsResponse.PendingLink(
                                a.getId(),
                                a.getAssignedUser() != null
                                        ? (a.getAssignedUser().getFirstName() + " " + a.getAssignedUser().getLastName()).trim()
                                        : null,
                                a.getAccessToken(),
                                a.getEndDate(),
                                a.getRecipientEmail()))
                        .collect(Collectors.toList());

        return new com.ces.exam.payload.response.ExamResultsResponse(exam.getTitle(), sessions, pendingLinks);
    }

    @Transactional(readOnly = true)
    public com.ces.exam.payload.response.ExamAnalyticsResponse getExamAnalytics(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        long completed = examSessionRepository.countByAssignment_Exam_IdAndStatus(id, SessionStatus.COMPLETED);
        Double avg = examSessionRepository.avgScoreForExam(id);
        long passed = examSessionRepository.countPassedForExam(id);
        BigDecimal avgScore = avg != null ? BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP) : null;
        BigDecimal passRate = completed > 0
                ? BigDecimal.valueOf(passed * 100.0 / completed).setScale(0, RoundingMode.HALF_UP)
                : null;

        // ---- score distribution (five 20-point buckets) ----
        int[] buckets = new int[5];
        for (BigDecimal s : examSessionRepository.completedScoresForExam(id)) {
            int idx = (int) Math.floor(s.doubleValue() / 20.0);
            if (idx < 0) idx = 0;
            if (idx > 4) idx = 4;
            buckets[idx]++;
        }
        String[] labels = {"0–20", "20–40", "40–60", "60–80", "80–100"};
        List<com.ces.exam.payload.response.ExamAnalyticsResponse.ScoreBucket> dist = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            dist.add(new com.ces.exam.payload.response.ExamAnalyticsResponse.ScoreBucket(labels[i], buckets[i]));
        }

        // ---- per-question stats + difficulty aggregation ----
        List<com.ces.exam.payload.response.ExamAnalyticsResponse.QuestionStat> qStats = new ArrayList<>();
        Map<String, long[]> diffAgg = new LinkedHashMap<>(); // difficulty -> [questionCount, correct, wrong]
        for (Object[] r : examSessionQuestionRepository.questionStatsForExam(id)) {
            Long qid = (Long) r[0];
            String text = (String) r[1];
            String type = r[2] != null ? r[2].toString() : null;
            String difficulty = r[3] != null ? r[3].toString() : null;
            long correct = ((Number) r[4]).longValue();
            long wrong = ((Number) r[5]).longValue();
            long pending = ((Number) r[6]).longValue();
            long total = ((Number) r[7]).longValue();
            long graded = correct + wrong;
            Double rate = graded > 0 ? Math.round(correct * 1000.0 / graded) / 10.0 : null;
            qStats.add(new com.ces.exam.payload.response.ExamAnalyticsResponse.QuestionStat(
                    qid, text, type, difficulty, correct, wrong, pending, total, rate));
            if (difficulty != null) {
                long[] a = diffAgg.computeIfAbsent(difficulty, k -> new long[3]);
                a[0] += 1;
                a[1] += correct;
                a[2] += wrong;
            }
        }
        // hardest first; questions with no graded answers go last
        qStats.sort(Comparator.comparing(q -> q.getSuccessRate() == null ? Double.MAX_VALUE : q.getSuccessRate()));

        List<com.ces.exam.payload.response.ExamAnalyticsResponse.DifficultyStat> diffStats = new ArrayList<>();
        for (String d : List.of("EASY", "MEDIUM", "HARD")) {
            long[] a = diffAgg.get(d);
            if (a == null) continue;
            long graded = a[1] + a[2];
            Double rate = graded > 0 ? Math.round(a[1] * 1000.0 / graded) / 10.0 : null;
            diffStats.add(new com.ces.exam.payload.response.ExamAnalyticsResponse.DifficultyStat(d, a[0], a[1], a[2], rate));
        }

        // ---- department comparison ----
        List<com.ces.exam.payload.response.ExamAnalyticsResponse.DepartmentStat> deptStats = new ArrayList<>();
        for (Object[] r : examSessionRepository.departmentStatsForExam(id)) {
            String name = (String) r[0];
            long count = ((Number) r[1]).longValue();
            BigDecimal dAvg = r[2] != null
                    ? BigDecimal.valueOf(((Number) r[2]).doubleValue()).setScale(1, RoundingMode.HALF_UP)
                    : null;
            deptStats.add(new com.ces.exam.payload.response.ExamAnalyticsResponse.DepartmentStat(name, count, dAvg));
        }

        return new com.ces.exam.payload.response.ExamAnalyticsResponse(
                exam.getTitle(), (int) completed, avgScore, passRate, exam.getPassMark(),
                dist, qStats, diffStats, deptStats);
    }

    @Transactional(readOnly = true)
    public com.ces.exam.payload.response.AnalyticsInsightsResponse getInsights() {
        List<com.ces.exam.payload.response.AnalyticsInsightsResponse.QuestionInsight> all = new ArrayList<>();
        for (Object[] r : examSessionQuestionRepository.questionStatsGlobal()) {
            Long qid = (Long) r[0];
            String text = (String) r[1];
            String type = r[2] != null ? r[2].toString() : null;
            String difficulty = r[3] != null ? r[3].toString() : null;
            long correct = ((Number) r[4]).longValue();
            long wrong = ((Number) r[5]).longValue();
            long total = ((Number) r[6]).longValue();
            long graded = correct + wrong;
            Double rate = graded > 0 ? Math.round(correct * 1000.0 / graded) / 10.0 : null;
            all.add(new com.ces.exam.payload.response.AnalyticsInsightsResponse.QuestionInsight(
                    qid, text, type, difficulty, correct, wrong, total, rate));
        }

        // hardest: lowest success rate first, ties broken by most answered
        List<com.ces.exam.payload.response.AnalyticsInsightsResponse.QuestionInsight> hardest = all.stream()
                .filter(q -> q.getSuccessRate() != null)
                .sorted(Comparator.comparing(
                        com.ces.exam.payload.response.AnalyticsInsightsResponse.QuestionInsight::getSuccessRate)
                        .thenComparing(Comparator.comparingLong(
                                com.ces.exam.payload.response.AnalyticsInsightsResponse.QuestionInsight::getTotal).reversed()))
                .limit(8)
                .collect(Collectors.toList());

        // most missed: highest absolute wrong count
        List<com.ces.exam.payload.response.AnalyticsInsightsResponse.QuestionInsight> mostMissed = all.stream()
                .filter(q -> q.getWrong() > 0)
                .sorted(Comparator.comparingLong(
                        com.ces.exam.payload.response.AnalyticsInsightsResponse.QuestionInsight::getWrong).reversed())
                .limit(8)
                .collect(Collectors.toList());

        List<com.ces.exam.payload.response.AnalyticsInsightsResponse.ViolationStat> vstats = new ArrayList<>();
        for (Object[] r : sessionViolationRepository.violationTypeStats()) {
            vstats.add(new com.ces.exam.payload.response.AnalyticsInsightsResponse.ViolationStat(
                    (String) r[0], (String) r[1], ((Number) r[2]).longValue()));
        }

        long totalViolations = sessionViolationRepository.count();
        long flagged = sessionViolationRepository.countDistinctFlaggedSessions();

        return new com.ces.exam.payload.response.AnalyticsInsightsResponse(
                hardest, mostMissed, vstats, totalViolations, flagged);
    }

    @Transactional
    public void deleteExam(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
        if (examSessionRepository.existsByAssignment_Exam_Id(id)) {
            throw new ValidationException("Bu imtahan üzrə iştirak/nəticələr var və silinə bilməz.");
        }
        examRepository.delete(exam);
    }

    @Transactional
    public ExamAssignmentResponse assignExam(ExamAssignmentRequest request) {
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        boolean link = "LINK".equalsIgnoreCase(request.getMode());

        ExamAssignment assignment = new ExamAssignment();
        assignment.setExam(exam);
        assignment.setStartDate(request.getStartDate());
        assignment.setEndDate(request.getEndDate());

        User assignedUser = null;
        if (request.getUserId() != null) {
            // Deliver to an existing platform user.
            assignedUser = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            assignment.setAssignedUser(assignedUser);
        } else if (link && request.getCandidateName() != null && !request.getCandidateName().isBlank()) {
            // Link pre-named for a specific candidate → create the account now.
            assignedUser = candidateService.create(request.getCandidateName().trim());
            assignment.setAssignedUser(assignedUser);
        } else if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            assignment.setAssignedDepartment(dept);
        } else if (!link) {
            throw new ValidationException("Təyinat hədəfi seçilməyib (istifadəçi və ya şöbə).");
        }
        // For link mode with no user/candidate, the link is anonymous — the taker
        // names themselves when they open it, and a candidate account is created then.

        // A shareable token is only minted for link delivery; internal stays dashboard-only.
        String recipientEmail = request.getRecipientEmail() != null && !request.getRecipientEmail().isBlank()
                ? request.getRecipientEmail().trim() : null;
        if (link) {
            assignment.setAccessToken(UUID.randomUUID().toString());
            assignment.setRecipientEmail(recipientEmail);
        }

        ExamAssignment saved = examAssignmentRepository.save(assignment);
        String candidateName = assignedUser != null
                ? (assignedUser.getFirstName() + " " + assignedUser.getLastName()).trim()
                : null;

        // Auto-send the invite when a link assignment carries a recipient e-mail.
        // Best-effort: a delivery failure must not roll back the (already valid) assignment.
        Boolean emailSent = null;
        if (link && recipientEmail != null) {
            emailSent = emailService.trySendExamInvite(
                    recipientEmail, candidateName, exam.getTitle(), saved.getAccessToken(), saved.getEndDate());
        }

        return new ExamAssignmentResponse(
                saved.getId(),
                saved.getAccessToken(),
                candidateName,
                exam.getTitle(),
                recipientEmail,
                emailSent
        );
    }

    /** Sends (or resends) the invite e-mail for an existing link assignment. */
    @Transactional
    public void sendInvite(Long assignmentId, String email) {
        ExamAssignment assignment = examAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Təyinat tapılmadı"));
        if (assignment.getAccessToken() == null) {
            throw new ValidationException("Bu təyinat link tipli deyil — e-poçt göndərilə bilməz.");
        }
        if (assignment.isConsumed()) {
            throw new ValidationException("Bu link artıq istifadə olunub.");
        }

        String target = email != null && !email.isBlank() ? email.trim() : assignment.getRecipientEmail();
        if (target == null || target.isBlank()) {
            throw new ValidationException("Alıcı e-poçt ünvanı göstərilməyib.");
        }

        String candidateName = assignment.getAssignedUser() != null
                ? (assignment.getAssignedUser().getFirstName() + " " + assignment.getAssignedUser().getLastName()).trim()
                : null;

        emailService.sendExamInvite(
                target, candidateName, assignment.getExam().getTitle(),
                assignment.getAccessToken(), assignment.getEndDate());

        // Remember where we sent it, for record-keeping and future resends.
        assignment.setRecipientEmail(target);
        examAssignmentRepository.save(assignment);
    }

    @Transactional
    public com.ces.exam.payload.response.BulkAssignmentResponse assignInternal(ExamAssignmentRequest request) {
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        List<Long> userIds = request.getUserIds() != null ? request.getUserIds() : List.of();
        List<Long> departmentIds = request.getDepartmentIds() != null ? request.getDepartmentIds() : List.of();
        if (userIds.isEmpty() && departmentIds.isEmpty()) {
            throw new ValidationException("Ən azı bir istifadəçi və ya şöbə seçin.");
        }

        int created = 0;
        int skipped = 0;

        for (Long userId : userIds) {
            if (examAssignmentRepository.existsByExamIdAndAssignedUserId(exam.getId(), userId)) {
                skipped++;
                continue;
            }
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            ExamAssignment assignment = new ExamAssignment();
            assignment.setExam(exam);
            assignment.setAssignedUser(user);
            assignment.setStartDate(request.getStartDate());
            assignment.setEndDate(request.getEndDate());
            examAssignmentRepository.save(assignment);
            created++;
        }

        for (Long departmentId : departmentIds) {
            if (examAssignmentRepository.existsByExamIdAndAssignedDepartmentId(exam.getId(), departmentId)) {
                skipped++;
                continue;
            }
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            ExamAssignment assignment = new ExamAssignment();
            assignment.setExam(exam);
            assignment.setAssignedDepartment(dept);
            assignment.setStartDate(request.getStartDate());
            assignment.setEndDate(request.getEndDate());
            examAssignmentRepository.save(assignment);
            created++;
        }

        return new com.ces.exam.payload.response.BulkAssignmentResponse(created, skipped);
    }

    private ExamResponse mapToResponse(Exam exam) {
        List<ExamResponse.ExamTopicConfigResponse> topicConfigs = null;
        if (exam.getTopicConfigs() != null && !exam.getTopicConfigs().isEmpty()) {
            topicConfigs = exam.getTopicConfigs().stream()
                    .map(tc -> new ExamResponse.ExamTopicConfigResponse(tc.getTopic().getId(), tc.getTopic().getName(), tc.getQuestionCount()))
                    .collect(Collectors.toList());
        }

        List<ExamResponse.ExamQuestionResponse> questions = null;
        int count;
        if (exam.getExamQuestions() != null && !exam.getExamQuestions().isEmpty()) {
            questions = exam.getExamQuestions().stream().map(eq -> {
                Question q = eq.getQuestion();
                List<QuestionOptionResponse> options = null;
                if (q.getOptions() != null) {
                    options = q.getOptions().stream()
                            .map(o -> new QuestionOptionResponse(o.getId(), o.getText(), o.getImageUrl(), o.getCorrect(), o.getSortOrder()))
                            .collect(Collectors.toList());
                }
                return new ExamResponse.ExamQuestionResponse(
                        q.getId(), q.getType().name(), q.getText(), q.getScore(),
                        q.getDifficulty() != null ? q.getDifficulty().name() : null,
                        q.getOwnerExam() == null, options);
            }).collect(Collectors.toList());
            count = questions.size();
        } else {
            count = topicConfigs != null
                    ? topicConfigs.stream().mapToInt(ExamResponse.ExamTopicConfigResponse::getQuestionCount).sum()
                    : 0;
        }

        return new ExamResponse(exam.getId(), exam.getTitle(), exam.getDescription(), exam.getType().name(),
                exam.getPassMark(), exam.getDurationMinutes(), count, topicConfigs, questions);
    }
}
