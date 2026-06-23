package com.ces.exam.repository;

import com.ces.exam.model.entity.ExamSession;
import com.ces.exam.model.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {
    Optional<ExamSession> findByAssignmentIdAndUserId(Long assignmentId, Long userId);

    List<ExamSession> findByUserIdAndAssignmentIdIn(Long userId, List<Long> assignmentIds);

    long countByStatusAndEndTimeBetween(SessionStatus status, LocalDateTime start, LocalDateTime end);

    List<ExamSession> findTop10ByStatusOrderByEndTimeDesc(SessionStatus status);

    @Query("SELECT s FROM ExamSession s " +
           "JOIN FETCH s.user u LEFT JOIN FETCH u.department " +
           "JOIN FETCH s.assignment a JOIN FETCH a.exam e " +
           "WHERE s.status = :status " +
           "AND (:departmentId IS NULL OR u.department.id = :departmentId) " +
           "AND (:examId IS NULL OR e.id = :examId) " +
           "AND (:startDate IS NULL OR s.endTime >= :startDate) " +
           "AND (:endDate IS NULL OR s.endTime <= :endDate) " +
           "ORDER BY s.endTime DESC")
    List<ExamSession> findCompletedReports(
            @Param("departmentId") Long departmentId,
            @Param("examId") Long examId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") SessionStatus status);

    default List<ExamSession> findCompletedReports(Long departmentId, Long examId,
                                                   LocalDateTime startDate, LocalDateTime endDate) {
        return findCompletedReports(departmentId, examId, startDate, endDate, SessionStatus.COMPLETED);
    }

    @Query("SELECT s FROM ExamSession s JOIN FETCH s.assignment a JOIN FETCH a.exam " +
           "JOIN FETCH s.sessionQuestions sq JOIN FETCH sq.question q LEFT JOIN FETCH q.options " +
           "WHERE s.id = :id")
    Optional<ExamSession> findByIdWithDetails(@Param("id") Long id);
}
