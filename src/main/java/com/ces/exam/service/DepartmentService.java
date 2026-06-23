package com.ces.exam.service;

import com.ces.exam.model.entity.Department;
import com.ces.exam.payload.request.DepartmentRequest;
import com.ces.exam.payload.response.DepartmentResponse;
import com.ces.exam.repository.DepartmentRepository;
import com.ces.exam.exception.ResourceNotFoundException;
import com.ces.exam.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(dep -> new DepartmentResponse(dep.getId(), dep.getName(), dep.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public DepartmentResponse createDepartment(DepartmentRequest request) {
        Department department = new Department();
        department.setName(request.getName());
        Department saved = departmentRepository.save(department);
        return new DepartmentResponse(saved.getId(), saved.getName(), saved.getCreatedAt());
    }
}
