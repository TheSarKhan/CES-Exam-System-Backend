package com.ces.exam.controller;

import com.ces.exam.payload.request.StartSessionRequest;
import com.ces.exam.payload.request.SubmitSessionRequest;
import com.ces.exam.payload.response.SessionResultResponse;
import com.ces.exam.payload.response.SessionStartResponse;
import com.ces.exam.service.ExamSessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sessions")
public class ExamSessionController {

    private final ExamSessionService examSessionService;

    public ExamSessionController(ExamSessionService examSessionService) {
        this.examSessionService = examSessionService;
    }

    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'CANDIDATE', 'ADMIN')")
    public ResponseEntity<SessionStartResponse> startSession(@Valid @RequestBody StartSessionRequest request) {
        return ResponseEntity.ok(examSessionService.startSession(request));
    }

    @GetMapping("/{sessionId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'CANDIDATE', 'ADMIN')")
    public ResponseEntity<SessionStartResponse> getActiveSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(examSessionService.getActiveSession(sessionId));
    }

    @PostMapping("/{sessionId}/submit")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'CANDIDATE', 'ADMIN')")
    public ResponseEntity<SessionResultResponse> submitSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitSessionRequest request) {
        return ResponseEntity.ok(examSessionService.submitSession(sessionId, request));
    }

    @GetMapping("/{sessionId}/result")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'CANDIDATE', 'ADMIN')")
    public ResponseEntity<SessionResultResponse> getResult(@PathVariable Long sessionId) {
        return ResponseEntity.ok(examSessionService.getSessionResult(sessionId));
    }
}
