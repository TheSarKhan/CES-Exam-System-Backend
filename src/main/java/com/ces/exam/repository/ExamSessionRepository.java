package com.ces.exam.repository;

import com.ces.exam.model.entity.ExamSession;
import com.ces.exam.model.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExamSessionRepository extends JpaRepository<ExamSession, Long>, JpaSpecificationExecutor<ExamSession> {
    Optional<ExamSession> findByAssignmentIdAndUserId(Long assignmentId, Long userId);

    List<ExamSession> findByUserIdAndAssignmentIdIn(Long userId, List<Long> assignmentIds);

    boolean existsByAssignment_Exam_Id(Long examId);

    long countByAssignment_Exam_IdAndStatus(Long examId, SessionStatus status);

    @Query("SELECT AVG(s.score) FROM ExamSession s WHERE s.assignment.exam.id = :examId " +
           "AND s.status = com.ces.exam.model.enums.SessionStatus.COMPLETED")
    Double avgScoreForExam(@Param("examId") Long examId);

    @Query("SELECT COUNT(s) FROM ExamSession s WHERE s.assignment.exam.id = :examId " +
           "AND s.status = com.ces.exam.model.enums.SessionStatus.COMPLETED AND s.passed = true")
    long countPassedForExam(@Param("examId") Long examId);

    @Query("SELECT s FROM ExamSession s JOIN FETCH s.user WHERE s.assignment.exam.id = :examId " +
           "ORDER BY s.startTime DESC")
    List<ExamSession> findByExamIdWithUser(@Param("examId") Long examId);

    long countByStatusAndEndTimeBetween(SessionStatus status, LocalDateTime start, LocalDateTime end);

    long countByStatusAndEndTimeAfter(SessionStatus status, LocalDateTime time);

    List<ExamSession> findTop10ByStatusOrderByEndTimeDesc(SessionStatus status);

    List<ExamSession> findTop20ByStatusOrderByEndTimeDesc(SessionStatus status);

    @Query("SELECT s FROM ExamSession s JOIN FETCH s.assignment a JOIN FETCH a.exam " +
           "JOIN FETCH s.sessionQuestions sq JOIN FETCH sq.question q LEFT JOIN FETCH q.options " +
           "WHERE s.id = :id")
    Optional<ExamSession> findByIdWithDetails(@Param("id") Long id);

    // Completed-session scores for an exam (for the score-distribution histogram).
    @Query("SELECT s.score FROM ExamSession s WHERE s.assignment.exam.id = :examId " +
           "AND s.status = com.ces.exam.model.enums.SessionStatus.COMPLETED AND s.score IS NOT NULL")
    List<BigDecimal> completedScoresForExam(@Param("examId") Long examId);

    // Per-department participation and average score (only users that have a department).
    // Returns: [departmentName, participantCount, avgScore]
    @Query("SELECT d.name, COUNT(s), AVG(s.score) FROM ExamSession s JOIN s.user u JOIN u.department d " +
           "WHERE s.assignment.exam.id = :examId " +
           "AND s.status = com.ces.exam.model.enums.SessionStatus.COMPLETED " +
           "GROUP BY d.name ORDER BY AVG(s.score) DESC")
    List<Object[]> departmentStatsForExam(@Param("examId") Long examId);

    // ---- dashboard-wide aggregates ----
    long countByStatus(SessionStatus status);

    @Query("SELECT AVG(s.score) FROM ExamSession s " +
           "WHERE s.status = com.ces.exam.model.enums.SessionStatus.COMPLETED")
    Double avgScoreAllCompleted();

    @Query("SELECT COUNT(s) FROM ExamSession s " +
           "WHERE s.status = com.ces.exam.model.enums.SessionStatus.COMPLETED AND s.passed = true")
    long countPassedAllCompleted();

    @Query("SELECT s.endTime FROM ExamSession s " +
           "WHERE s.status = com.ces.exam.model.enums.SessionStatus.COMPLETED AND s.endTime >= :since")
    List<LocalDateTime> completedEndTimesSince(@Param("since") LocalDateTime since);

    // ---- personal progress (one employee) ----
    @Query("SELECT s FROM ExamSession s JOIN FETCH s.assignment a JOIN FETCH a.exam e " +
           "WHERE s.user.id = :userId AND s.status = com.ces.exam.model.enums.SessionStatus.COMPLETED " +
           "AND e.type = com.ces.exam.model.enums.ExamType.EXAM ORDER BY s.endTime ASC")
    List<ExamSession> findCompletedExamSessionsForUser(@Param("userId") Long userId);

    @Query("SELECT AVG(s.score) FROM ExamSession s " +
           "WHERE s.user.id = :userId AND s.status = com.ces.exam.model.enums.SessionStatus.COMPLETED " +
           "AND s.assignment.exam.type = com.ces.exam.model.enums.ExamType.EXAM")
    Double avgScoreForUser(@Param("userId") Long userId);

    @Query("SELECT AVG(s.score) FROM ExamSession s " +
           "WHERE s.user.department.id = :deptId AND s.status = com.ces.exam.model.enums.SessionStatus.COMPLETED " +
           "AND s.assignment.exam.type = com.ces.exam.model.enums.ExamType.EXAM")
    Double avgScoreForDepartment(@Param("deptId") Long deptId);

    // ---- department detail (per-member exam stats) ----
    // Returns one row per member with completed-exam activity:
    // [userId, completedCount, avgScore, lastEndTime, passedCount]
    @Query("SELECT s.user.id, COUNT(s), AVG(s.score), MAX(s.endTime), " +
           "SUM(CASE WHEN s.passed = true THEN 1 ELSE 0 END) " +
           "FROM ExamSession s WHERE s.user.department.id = :deptId " +
           "AND s.status = com.ces.exam.model.enums.SessionStatus.COMPLETED " +
           "AND s.assignment.exam.type = com.ces.exam.model.enums.ExamType.EXAM " +
           "GROUP BY s.user.id")
    List<Object[]> memberExamStatsForDepartment(@Param("deptId") Long deptId);
}
