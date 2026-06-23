package com.ces.exam.service;

import com.ces.exam.model.entity.ExamSession;
import com.ces.exam.model.entity.User;
import com.ces.exam.payload.response.ExamReportResponse;
import com.ces.exam.repository.ExamSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ExamSessionRepository sessionRepository;

    public ReportService(ExamSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional(readOnly = true)
    public List<ExamReportResponse> getReports(Long departmentId, Long examId,
                                               LocalDateTime startDate, LocalDateTime endDate) {
        return sessionRepository.findCompletedReports(departmentId, examId, startDate, endDate)
                .stream()
                .map(this::mapToReport)
                .collect(Collectors.toList());
    }

    private ExamReportResponse mapToReport(ExamSession session) {
        User user = session.getUser();
        String departmentName = user.getDepartment() != null ? user.getDepartment().getName() : null;
        var exam = session.getAssignment().getExam();

        return new ExamReportResponse(
                session.getId(),
                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                departmentName,
                exam.getId(),
                exam.getTitle(),
                exam.getType().name(),
                session.getScore(),
                session.getPassed(),
                session.getStartTime(),
                session.getEndTime()
        );
    }
}
