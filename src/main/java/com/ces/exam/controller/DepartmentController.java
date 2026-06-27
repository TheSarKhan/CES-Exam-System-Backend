package com.ces.exam.controller;

import com.ces.exam.payload.request.DepartmentRequest;
import com.ces.exam.payload.response.DepartmentDetailResponse;
import com.ces.exam.payload.response.DepartmentResponse;
import com.ces.exam.service.DepartmentService;
import com.ces.exam.util.PageRequests;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // Full list by default; pass ?page=N (optionally &size=) for a paginated envelope.
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<?> getAllDepartments(@RequestParam(required = false) Integer page,
                                               @RequestParam(required = false) Integer size) {
        if (page == null) return ResponseEntity.ok(departmentService.getAllDepartments());
        return ResponseEntity.ok(departmentService.getAllDepartments(PageRequests.of(page, size)));
    }

    @GetMapping("/{id}/detail")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentDetailResponse> getDepartmentDetail(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.createDepartment(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> updateDepartment(@PathVariable Long id,
                                                               @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
