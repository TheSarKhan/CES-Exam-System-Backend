package com.ces.exam.repository;

import com.ces.exam.model.entity.ExamSessionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamSessionQuestionRepository extends JpaRepository<ExamSessionQuestion, Long> {
    boolean existsByQuestionId(Long questionId);

    // Open-ended answers in a session still awaiting manual grading. Limited to real
    // exams (surveys are never graded) — caller should only pass completed sessions.
    @Query("SELECT COUNT(sq) FROM ExamSessionQuestion sq " +
           "WHERE sq.session.id = :sessionId AND sq.isCorrect IS NULL " +
           "AND sq.session.assignment.exam.type = com.ces.exam.model.enums.ExamType.EXAM")
    long countPendingGradingForSession(@Param("sessionId") Long sessionId);

    // Pending-grading counts for every session of an exam in ONE query (avoids N+1
    // on the results page). Returns rows of [sessionId, count].
    @Query("SELECT sq.session.id, COUNT(sq) FROM ExamSessionQuestion sq " +
           "WHERE sq.session.assignment.exam.id = :examId AND sq.isCorrect IS NULL " +
           "AND sq.session.assignment.exam.type = com.ces.exam.model.enums.ExamType.EXAM " +
           "GROUP BY sq.session.id")
    List<Object[]> pendingGradingCountsForExam(@Param("examId") Long examId);

    // Per-question outcome aggregation across all COMPLETED sessions of an exam.
    // Returns: [questionId, text, type, difficulty, correct, wrong, pending, total]
    @Query("SELECT q.id, q.text, q.type, q.difficulty, " +
           "SUM(CASE WHEN sq.isCorrect = true THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN sq.isCorrect = false THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN sq.isCorrect IS NULL THEN 1 ELSE 0 END), " +
           "COUNT(sq) " +
           "FROM ExamSessionQuestion sq JOIN sq.question q " +
           "WHERE sq.session.assignment.exam.id = :examId " +
           "AND sq.session.status = com.ces.exam.model.enums.SessionStatus.COMPLETED " +
           "GROUP BY q.id, q.text, q.type, q.difficulty")
    List<Object[]> questionStatsForExam(@Param("examId") Long examId);

    // System-wide per-question outcome aggregation across ALL completed real-exam sessions.
    // Returns: [questionId, text, type, difficulty, correct, wrong, total]
    @Query("SELECT q.id, q.text, q.type, q.difficulty, " +
           "SUM(CASE WHEN sq.isCorrect = true THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN sq.isCorrect = false THEN 1 ELSE 0 END), " +
           "COUNT(sq) " +
           "FROM ExamSessionQuestion sq JOIN sq.question q " +
           "WHERE sq.session.status = com.ces.exam.model.enums.SessionStatus.COMPLETED " +
           "AND sq.session.assignment.exam.type = com.ces.exam.model.enums.ExamType.EXAM " +
           "GROUP BY q.id, q.text, q.type, q.difficulty")
    List<Object[]> questionStatsGlobal();

    // Completed exam sessions that still have open-ended answers awaiting manual grading.
    // Returns: [sessionId, firstName, lastName, examId, examTitle, pendingCount]
    @Query("SELECT s.id, u.firstName, u.lastName, e.id, e.title, COUNT(sq) " +
           "FROM ExamSessionQuestion sq JOIN sq.session s JOIN s.user u JOIN s.assignment a JOIN a.exam e " +
           "WHERE sq.isCorrect IS NULL AND s.status = com.ces.exam.model.enums.SessionStatus.COMPLETED " +
           "AND e.type = com.ces.exam.model.enums.ExamType.EXAM " +
           "GROUP BY s.id, u.firstName, u.lastName, e.id, e.title " +
           "ORDER BY COUNT(sq) DESC")
    List<Object[]> pendingGradingSessions();

    // Per-category correctness for one user across their completed sessions.
    // Inline questions (no topic) are excluded by the inner joins. Returns: [categoryName, correct, graded]
    @Query("SELECT c.name, " +
           "SUM(CASE WHEN sq.isCorrect = true THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN sq.isCorrect IS NOT NULL THEN 1 ELSE 0 END) " +
           "FROM ExamSessionQuestion sq JOIN sq.session s JOIN sq.question q JOIN q.topic t JOIN t.category c " +
           "WHERE s.user.id = :userId AND s.status = com.ces.exam.model.enums.SessionStatus.COMPLETED " +
           "GROUP BY c.name ORDER BY c.name")
    List<Object[]> categoryStatsForUser(@Param("userId") Long userId);
}
