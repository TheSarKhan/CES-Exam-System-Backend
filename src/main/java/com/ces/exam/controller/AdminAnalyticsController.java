package com.ces.exam.controller;

import com.ces.exam.payload.response.AnalyticsInsightsResponse;
import com.ces.exam.service.ExamService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final ExamService examService;

    public AdminAnalyticsController(ExamService examService) {
        this.examService = examService;
    }

    // System-wide question difficulty + anti-cheat aggregation for the global analytics page.
    @GetMapping("/insights")
    public ResponseEntity<AnalyticsInsightsResponse> getInsights() {
        return ResponseEntity.ok(examService.getInsights());
    }
}
