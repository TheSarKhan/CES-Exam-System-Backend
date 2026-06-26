package com.ces.exam.controller;

import com.ces.exam.payload.request.StartByTokenRequest;
import com.ces.exam.payload.request.SubmitSessionRequest;
import com.ces.exam.payload.response.SessionResultResponse;
import com.ces.exam.payload.response.SessionStartResponse;
import com.ces.exam.payload.response.TokenAssignmentResponse;
import com.ces.exam.service.ExamSessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/exam-token")
public class PublicExamTokenController {

    private final ExamSessionService examSessionService;

    public PublicExamTokenController(ExamSessionService examSessionService) {
        this.examSessionService = examSessionService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<TokenAssignmentResponse> getAssignment(@PathVariable String token) {
        return ResponseEntity.ok(examSessionService.getAssignmentByToken(token));
    }

    @PostMapping("/{token}/start")
    public ResponseEntity<SessionStartResponse> startSession(
            @PathVariable String token,
            @RequestBody(required = false) StartByTokenRequest request) {
        String candidateName = request != null ? request.getCandidateName() : null;
        return ResponseEntity.ok(examSessionService.startSessionByToken(token, candidateName));
    }

    @GetMapping("/{token}/sessions/{sessionId}")
    public ResponseEntity<SessionStartResponse> getActiveSession(
            @PathVariable String token,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(examSessionService.getActiveSessionByToken(token, sessionId));
    }

    @PostMapping("/{token}/sessions/{sessionId}/submit")
    public ResponseEntity<SessionResultResponse> submitSession(
            @PathVariable String token,
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitSessionRequest request) {
        return ResponseEntity.ok(examSessionService.submitSessionByToken(token, sessionId, request));
    }

    @GetMapping("/{token}/sessions/{sessionId}/result")
    public ResponseEntity<SessionResultResponse> getResult(
            @PathVariable String token,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(examSessionService.getSessionResultByToken(token, sessionId));
    }
}
