package com.ces.exam.controller;

import com.ces.exam.payload.request.ExamAssignmentRequest;
import com.ces.exam.payload.request.ExamRequest;
import com.ces.exam.payload.request.GradeSessionRequest;
import com.ces.exam.payload.request.SendInviteRequest;
import com.ces.exam.payload.response.BulkAssignmentResponse;
import com.ces.exam.payload.response.ExamAnalyticsResponse;
import com.ces.exam.payload.response.ExamAssignmentResponse;
import com.ces.exam.payload.response.ExamResponse;
import com.ces.exam.payload.response.ExamResultsResponse;
import com.ces.exam.payload.response.SessionResultResponse;
import com.ces.exam.payload.response.ViolationResponse;
import com.ces.exam.service.ExamService;
import com.ces.exam.service.ExamSessionService;
import com.ces.exam.util.PageRequests;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@PreAuthorize("hasRole('ADMIN')")
public class ExamController {

    private final ExamService examService;
    private final ExamSessionService examSessionService;

    public ExamController(ExamService examService, ExamSessionService examSessionService) {
        this.examService = examService;
        this.examSessionService = examSessionService;
    }

    // Full list by default; pass ?page=N (optionally &size=) for a paginated envelope.
    @GetMapping
    public ResponseEntity<?> getAllExams(@RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size) {
        if (page == null) return ResponseEntity.ok(examService.getAllExams());
        return ResponseEntity.ok(examService.getAllExams(PageRequests.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamResponse> getExam(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getExam(id));
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<ExamResultsResponse> getExamResults(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getExamResults(id));
    }

    @GetMapping("/{id}/analytics")
    public ResponseEntity<ExamAnalyticsResponse> getExamAnalytics(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getExamAnalytics(id));
    }

    @GetMapping("/sessions/{sessionId}/result")
    public ResponseEntity<SessionResultResponse> getSessionResult(@PathVariable Long sessionId) {
        return ResponseEntity.ok(examSessionService.getSessionResultForAdmin(sessionId));
    }

    @PutMapping("/sessions/{sessionId}/grade")
    public ResponseEntity<SessionResultResponse> gradeSession(
            @PathVariable Long sessionId, @Valid @RequestBody GradeSessionRequest request) {
        return ResponseEntity.ok(examSessionService.gradeSession(sessionId, request));
    }

    @GetMapping("/sessions/{sessionId}/violations")
    public ResponseEntity<List<ViolationResponse>> getSessionViolations(@PathVariable Long sessionId) {
        return ResponseEntity.ok(examSessionService.getSessionViolations(sessionId));
    }

    @PostMapping
    public ResponseEntity<ExamResponse> createExam(@Valid @RequestBody ExamRequest request) {
        return ResponseEntity.ok(examService.createExam(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamResponse> updateExam(@PathVariable Long id, @Valid @RequestBody ExamRequest request) {
        return ResponseEntity.ok(examService.updateExam(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign")
    public ResponseEntity<ExamAssignmentResponse> assignExam(@Valid @RequestBody ExamAssignmentRequest request) {
        return ResponseEntity.ok(examService.assignExam(request));
    }

    @PostMapping("/assign-internal")
    public ResponseEntity<BulkAssignmentResponse> assignInternal(@Valid @RequestBody ExamAssignmentRequest request) {
        return ResponseEntity.ok(examService.assignInternal(request));
    }

    @PostMapping("/assignments/{assignmentId}/send-invite")
    public ResponseEntity<Void> sendInvite(@PathVariable Long assignmentId,
                                           @Valid @RequestBody SendInviteRequest request) {
        examService.sendInvite(assignmentId, request.getEmail());
        return ResponseEntity.noContent().build();
    }
}
