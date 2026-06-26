package com.ces.exam.controller;

import com.ces.exam.payload.request.ChangePasswordRequest;
import com.ces.exam.payload.request.UpdateProfileRequest;
import com.ces.exam.payload.response.AccountResponse;
import com.ces.exam.payload.response.EmployeeNotificationResponse;
import com.ces.exam.payload.response.ProgressResponse;
import com.ces.exam.service.AccountService;
import com.ces.exam.service.EmployeeNotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/account")
@PreAuthorize("hasAnyRole('EMPLOYEE', 'CANDIDATE', 'ADMIN')")
public class AccountController {

    private final AccountService accountService;
    private final EmployeeNotificationService notificationService;

    public AccountController(AccountService accountService,
                            EmployeeNotificationService notificationService) {
        this.accountService = accountService;
        this.notificationService = notificationService;
    }

    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getMyProfile() {
        return ResponseEntity.ok(accountService.getMyProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<AccountResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(accountService.updateProfile(request));
    }

    @PostMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        accountService.changePassword(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/progress")
    public ResponseEntity<ProgressResponse> getProgress() {
        return ResponseEntity.ok(accountService.getProgress());
    }

    @GetMapping("/notifications")
    public ResponseEntity<EmployeeNotificationResponse> getNotifications() {
        return ResponseEntity.ok(notificationService.getFeed());
    }

    @PostMapping("/notifications/read")
    public ResponseEntity<Void> markNotificationsRead() {
        notificationService.markAllRead();
        return ResponseEntity.noContent().build();
    }
}
