package com.ces.exam.repository;

import com.ces.exam.model.entity.ExamAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExamAssignmentRepository extends JpaRepository<ExamAssignment, Long> {

    @Query("SELECT COUNT(a) FROM ExamAssignment a WHERE " +
           "(a.endDate IS NULL OR a.endDate >= :now) AND " +
           "(a.startDate IS NULL OR a.startDate <= :now)")
    long countActiveAssignments(@Param("now") LocalDateTime now);

    @Query("SELECT a FROM ExamAssignment a JOIN FETCH a.exam e WHERE " +
           "a.assignedUser.id = :userId OR " +
           "(a.assignedDepartment.id IS NOT NULL AND a.assignedDepartment.id = :departmentId)")
    List<ExamAssignment> findMyAssignments(@Param("userId") Long userId, @Param("departmentId") Long departmentId);

    @Query("SELECT a FROM ExamAssignment a JOIN FETCH a.exam LEFT JOIN FETCH a.assignedUser " +
           "WHERE a.accessToken = :token")
    Optional<ExamAssignment> findByAccessToken(@Param("token") String token);

    boolean existsByExamIdAndAssignedUserId(Long examId, Long userId);

    boolean existsByExamIdAndAssignedDepartmentId(Long examId, Long departmentId);

    long countByExamId(Long examId);

    @Query("SELECT a FROM ExamAssignment a LEFT JOIN FETCH a.assignedUser " +
           "WHERE a.exam.id = :examId AND a.accessToken IS NOT NULL AND a.consumed = false " +
           "ORDER BY a.createdAt DESC")
    List<ExamAssignment> findPendingLinks(@Param("examId") Long examId);

    // Recipient e-mails that already used (consumed) a link for this exam — their remaining
    // unused links are redundant and should be hidden from the "unused links" list.
    @Query("SELECT DISTINCT LOWER(a.recipientEmail) FROM ExamAssignment a " +
           "WHERE a.exam.id = :examId AND a.consumed = true AND a.recipientEmail IS NOT NULL")
    List<String> findUsedRecipientEmails(@Param("examId") Long examId);
}
