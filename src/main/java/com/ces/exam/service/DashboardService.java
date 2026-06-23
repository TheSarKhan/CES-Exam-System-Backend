package com.ces.exam.service;

import com.ces.exam.model.entity.ExamSession;
import com.ces.exam.model.enums.SessionStatus;
import com.ces.exam.payload.response.DashboardStatsResponse;
import com.ces.exam.payload.response.RecentExamSessionResponse;
import com.ces.exam.repository.ExamAssignmentRepository;
import com.ces.exam.repository.ExamSessionRepository;
import com.ces.exam.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final ExamAssignmentRepository assignmentRepository;
    private final ExamSessionRepository sessionRepository;

    public DashboardService(UserRepository userRepository,
                            ExamAssignmentRepository assignmentRepository,
                            ExamSessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        LocalDateTime now = LocalDateTime.now();
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        long totalUsers = userRepository.countByStatus("ACTIVE");
        long activeExams = assignmentRepository.countActiveAssignments(now);
        long completedThisMonth = sessionRepository.countByStatusAndEndTimeBetween(
                SessionStatus.COMPLETED, monthStart, monthEnd);

        List<RecentExamSessionResponse> recent = sessionRepository
                .findTop10ByStatusOrderByEndTimeDesc(SessionStatus.COMPLETED)
                .stream()
                .map(this::mapRecentSession)
                .collect(Collectors.toList());

        return new DashboardStatsResponse(totalUsers, activeExams, completedThisMonth, recent);
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
