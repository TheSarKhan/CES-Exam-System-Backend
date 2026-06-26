package com.ces.exam.controller;

import com.ces.exam.payload.request.BulkQuestionRequest;
import com.ces.exam.payload.request.CategoryRequest;
import com.ces.exam.payload.request.QuestionRequest;
import com.ces.exam.payload.request.TopicRequest;
import com.ces.exam.payload.response.BulkImportResponse;
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

    // ---------- CATEGORIES ----------

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(questionBankService.getCategories(departmentId));
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(questionBankService.getCategory(id));
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(questionBankService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id,
                                                           @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(questionBankService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        questionBankService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories/{categoryId}/topics")
    public ResponseEntity<List<TopicResponse>> getTopicsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(questionBankService.getTopicsByCategory(categoryId));
    }

    @GetMapping("/categories/{categoryId}/questions")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(questionBankService.getQuestionsByCategory(categoryId));
    }

    // ---------- TOPICS ----------

    @PostMapping("/topics")
    public ResponseEntity<TopicResponse> createTopic(@Valid @RequestBody TopicRequest request) {
        return ResponseEntity.ok(questionBankService.createTopic(request));
    }

    @PutMapping("/topics/{id}")
    public ResponseEntity<TopicResponse> updateTopic(@PathVariable Long id,
                                                     @Valid @RequestBody TopicRequest request) {
        return ResponseEntity.ok(questionBankService.updateTopic(id, request));
    }

    @DeleteMapping("/topics/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        questionBankService.deleteTopic(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/topics/{topicId}/questions")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByTopic(@PathVariable Long topicId) {
        return ResponseEntity.ok(questionBankService.getQuestionsByTopic(topicId));
    }

    // ---------- QUESTIONS ----------

    @GetMapping("/questions/{id}")
    public ResponseEntity<QuestionResponse> getQuestion(@PathVariable Long id) {
        return ResponseEntity.ok(questionBankService.getQuestion(id));
    }

    @PostMapping("/questions")
    public ResponseEntity<QuestionResponse> createQuestion(@Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionBankService.createQuestion(request));
    }

    @PostMapping("/questions/bulk")
    public ResponseEntity<BulkImportResponse> bulkCreate(@Valid @RequestBody BulkQuestionRequest request) {
        return ResponseEntity.ok(questionBankService.bulkCreate(request));
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<QuestionResponse> updateQuestion(@PathVariable Long id,
                                                           @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionBankService.updateQuestion(id, request));
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionBankService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
