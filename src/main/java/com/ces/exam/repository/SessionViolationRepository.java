package com.ces.exam.repository;

import com.ces.exam.model.entity.SessionViolation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SessionViolationRepository extends JpaRepository<SessionViolation, Long> {
    List<SessionViolation> findBySessionIdOrderByOccurredAtAsc(Long sessionId);
    long countBySessionId(Long sessionId);

    // Violation counts for every session of an exam in ONE query (avoids N+1 on the
    // results page). Returns rows of [sessionId, count].
    @Query("SELECT v.session.id, COUNT(v) FROM SessionViolation v " +
           "WHERE v.session.assignment.exam.id = :examId GROUP BY v.session.id")
    List<Object[]> violationCountsForExam(@Param("examId") Long examId);

    // Sessions that recorded proctoring violations, most-flagged first.
    // Returns: [sessionId, firstName, lastName, examId, examTitle, violationCount]
    @Query("SELECT s.id, u.firstName, u.lastName, e.id, e.title, COUNT(v) " +
           "FROM SessionViolation v JOIN v.session s JOIN s.user u JOIN s.assignment a JOIN a.exam e " +
           "GROUP BY s.id, u.firstName, u.lastName, e.id, e.title " +
           "ORDER BY COUNT(v) DESC")
    List<Object[]> flaggedSessions();

    // Violation totals by type (the anti-cheat log summary). Returns: [type, label, count]
    @Query("SELECT v.type, MAX(v.label), COUNT(v) FROM SessionViolation v " +
           "GROUP BY v.type ORDER BY COUNT(v) DESC")
    List<Object[]> violationTypeStats();

    @Query("SELECT COUNT(DISTINCT v.session.id) FROM SessionViolation v")
    long countDistinctFlaggedSessions();
}
