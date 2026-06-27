package com.ces.exam.controller;

import com.ces.exam.payload.request.UserRequest;
import com.ces.exam.payload.response.UserResponse;
import com.ces.exam.service.UserService;
import com.ces.exam.util.PageRequests;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Returns the full list by default (legacy); pass ?page=N for a paginated envelope.
    @GetMapping
    public ResponseEntity<?> getAllUsers(@RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size) {
        if (page == null) return ResponseEntity.ok(userService.getAllUsers());
        return ResponseEntity.ok(userService.getAllUsers(PageRequests.of(page, size)));
    }

    // Server-side search/filter/paginate for the admin users table.
    // role: ALL | PLATFORM | ADMIN | EMPLOYEE | CANDIDATE
    @GetMapping("/search")
    public ResponseEntity<org.springframework.data.domain.Page<com.ces.exam.payload.response.UserResponse>> searchUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(userService.searchUsers(search, status, departmentId, role, PageRequests.of(page, size)));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody com.ces.exam.payload.request.UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody com.ces.exam.payload.request.ResetPasswordRequest request) {
        userService.resetPassword(id, request.getPassword());
        return ResponseEntity.ok().build();
    }
}
