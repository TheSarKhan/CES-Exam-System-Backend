package com.ces.exam.service;

import com.ces.exam.model.entity.ExamSession;
import com.ces.exam.model.enums.SessionStatus;
import com.ces.exam.payload.response.DashboardStatsResponse;
import com.ces.exam.payload.response.DashboardStatsResponse.AttentionSession;
import com.ces.exam.payload.response.DashboardStatsResponse.DayActivity;
import com.ces.exam.payload.response.RecentExamSessionResponse;
import com.ces.exam.repository.ExamAssignmentRepository;
import com.ces.exam.repository.ExamRepository;
import com.ces.exam.repository.ExamSessionQuestionRepository;
import com.ces.exam.repository.ExamSessionRepository;
import com.ces.exam.repository.SessionViolationRepository;
import com.ces.exam.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final ExamAssignmentRepository assignmentRepository;
    private final ExamSessionRepository sessionRepository;
    private final ExamSessionQuestionRepository sessionQuestionRepository;
    private final SessionViolationRepository violationRepository;

    public DashboardService(UserRepository userRepository,
                            ExamRepository examRepository,
                            ExamAssignmentRepository assignmentRepository,
                            ExamSessionRepository sessionRepository,
                            ExamSessionQuestionRepository sessionQuestionRepository,
                            SessionViolationRepository violationRepository) {
        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.assignmentRepository = assignmentRepository;
        this.sessionRepository = sessionRepository;
        this.sessionQuestionRepository = sessionQuestionRepository;
        this.violationRepository = violationRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        LocalDateTime now = LocalDateTime.now();
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        long totalUsers = userRepository.countByStatus("ACTIVE");
        long totalExams = examRepository.count();
        long activeExams = assignmentRepository.countActiveAssignments(now);
        long completedThisMonth = sessionRepository.countByStatusAndEndTimeBetween(
                SessionStatus.COMPLETED, monthStart, monthEnd);
        long completedTotal = sessionRepository.countByStatus(SessionStatus.COMPLETED);

        Double avg = sessionRepository.avgScoreAllCompleted();
        BigDecimal avgScore = avg != null ? BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP) : null;
        long passedTotal = sessionRepository.countPassedAllCompleted();
        BigDecimal passRate = completedTotal > 0
                ? BigDecimal.valueOf(passedTotal * 100.0 / completedTotal).setScale(0, RoundingMode.HALF_UP)
                : null;

        // ---- attention: pending manual grading ----
        List<Object[]> pgRows = sessionQuestionRepository.pendingGradingSessions();
        List<AttentionSession> pendingGrading = pgRows.stream().limit(6)
                .map(this::mapAttention).collect(Collectors.toList());

        // ---- attention: proctoring violations ----
        List<Object[]> flaggedRows = violationRepository.flaggedSessions();
        List<AttentionSession> flaggedSessions = flaggedRows.stream().limit(6)
                .map(this::mapAttention).collect(Collectors.toList());

        // ---- weekly activity (completions per day, last 7 days) ----
        LocalDate today = LocalDate.now();
        List<LocalDateTime> ends = sessionRepository.completedEndTimesSince(today.minusDays(6).atStartOfDay());
        Map<LocalDate, Long> byDay = ends.stream()
                .filter(e -> e != null)
                .collect(Collectors.groupingBy(LocalDateTime::toLocalDate, Collectors.counting()));
        List<DayActivity> weekly = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String label = String.format("%02d.%02d", d.getDayOfMonth(), d.getMonthValue());
            weekly.add(new DayActivity(label, byDay.getOrDefault(d, 0L)));
        }

        // ---- recent completed sessions ----
        List<RecentExamSessionResponse> recent = sessionRepository
                .findTop10ByStatusOrderByEndTimeDesc(SessionStatus.COMPLETED)
                .stream()
                .map(this::mapRecentSession)
                .collect(Collectors.toList());

        return new DashboardStatsResponse(
                totalUsers, totalExams, activeExams, completedThisMonth, completedTotal,
                avgScore, passRate, pgRows.size(), flaggedRows.size(),
                recent, pendingGrading, flaggedSessions, weekly);
    }

    private AttentionSession mapAttention(Object[] r) {
        String userName = ((String) r[1] + " " + (String) r[2]).trim();
        return new AttentionSession((Long) r[0], userName, (Long) r[3], (String) r[4], ((Number) r[5]).intValue());
    }

    private RecentExamSessionResponse mapRecentSession(ExamSession session) {
        String userName = session.getUser().getFirstName() + " " + session.getUser().getLastName();
        String examTitle = session.getAssignment().getExam().getTitle();
        return new RecentExamSessionResponse(
                session.getId(),
                userName,
                examTitle,
                session.getScore(),
                session.getPassed(),
                session.getEndTime()
        );
    }
}
