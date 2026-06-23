package com.ces.exam.controller;

import com.ces.exam.payload.request.CategoryRequest;
import com.ces.exam.payload.request.QuestionRequest;
import com.ces.exam.payload.request.TopicRequest;
import com.ces.exam.payload.response.CategoryResponse;
import com.ces.exam.payload.response.QuestionResponse;
import com.ces.exam.payload.response.TopicResponse;
import com.ces.exam.service.QuestionBankService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/question-bank")
@PreAuthorize("hasRole('ADMIN')")
public class QuestionBankController {

    private final QuestionBankService questionBankService;

    public QuestionBankController(QuestionBankService questionBankService) {
        this.questionBankService = questionBankService;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(questionBankService.getAllCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(questionBankService.createCategory(request));
    }

    @GetMapping("/categories/{categoryId}/topics")
    public ResponseEntity<List<TopicResponse>> getTopicsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(questionBankService.getTopicsByCategory(categoryId));
    }

    @PostMapping("/topics")
    public ResponseEntity<TopicResponse> createTopic(@Valid @RequestBody TopicRequest request) {
        return ResponseEntity.ok(questionBankService.createTopic(request));
    }

    @GetMapping("/topics/{topicId}/questions")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByTopic(@PathVariable Long topicId) {
        return ResponseEntity.ok(questionBankService.getQuestionsByTopic(topicId));
    }

    @PostMapping("/questions")
    public ResponseEntity<QuestionResponse> createQuestion(@Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionBankService.createQuestion(request));
    }
}
