package com.ces.exam.service;

import com.ces.exam.model.entity.ExamSession;
import com.ces.exam.model.entity.User;
import com.ces.exam.model.enums.SessionStatus;
import com.ces.exam.payload.response.ExamReportResponse;
import com.ces.exam.repository.ExamSessionRepository;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        Specification<ExamSession> spec = (root, query, cb) -> {
            // Eager-load the associations the report mapping needs (avoids N+1).
            Fetch<?, ?> userFetch = root.fetch("user", JoinType.INNER);
            Fetch<?, ?> deptFetch = userFetch.fetch("department", JoinType.LEFT);
            Fetch<?, ?> assignmentFetch = root.fetch("assignment", JoinType.INNER);
            Fetch<?, ?> examFetch = assignmentFetch.fetch("exam", JoinType.INNER);

            // Reuse the fetch joins for filtering instead of emitting untyped
            // ":param IS NULL OR ..." predicates, which PostgreSQL cannot type.
            Join<?, ?> deptJoin = (Join<?, ?>) deptFetch;
            Join<?, ?> examJoin = (Join<?, ?>) examFetch;

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), SessionStatus.COMPLETED));
            if (departmentId != null) {
                predicates.add(cb.equal(deptJoin.get("id"), departmentId));
            }
            if (examId != null) {
                predicates.add(cb.equal(examJoin.get("id"), examId));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("endTime"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.<LocalDateTime>get("endTime"), endDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return sessionRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "endTime"))
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
