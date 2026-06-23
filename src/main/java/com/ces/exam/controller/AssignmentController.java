package com.ces.exam.controller;

import com.ces.exam.payload.response.MyAssignmentResponse;
import com.ces.exam.service.ExamSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {

    private final ExamSessionService examSessionService;

    public AssignmentController(ExamSessionService examSessionService) {
        this.examSessionService = examSessionService;
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'CANDIDATE', 'ADMIN')")
    public ResponseEntity<List<MyAssignmentResponse>> getMyAssignments() {
        return ResponseEntity.ok(examSessionService.getMyAssignments());
    }
}
