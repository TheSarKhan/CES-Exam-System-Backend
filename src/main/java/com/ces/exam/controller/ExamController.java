package com.ces.exam.controller;

import com.ces.exam.payload.request.ExamAssignmentRequest;
import com.ces.exam.payload.request.ExamRequest;
import com.ces.exam.payload.response.ExamAssignmentResponse;
import com.ces.exam.payload.response.ExamResponse;
import com.ces.exam.service.ExamService;
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

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public ResponseEntity<List<ExamResponse>> getAllExams() {
        return ResponseEntity.ok(examService.getAllExams());
    }

    @PostMapping
    public ResponseEntity<ExamResponse> createExam(@Valid @RequestBody ExamRequest request) {
        return ResponseEntity.ok(examService.createExam(request));
    }

    @PostMapping("/assign")
    public ResponseEntity<ExamAssignmentResponse> assignExam(@Valid @RequestBody ExamAssignmentRequest request) {
        return ResponseEntity.ok(examService.assignExam(request));
    }
}
