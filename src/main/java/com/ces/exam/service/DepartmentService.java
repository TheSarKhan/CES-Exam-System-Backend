package com.ces.exam.service;

import com.ces.exam.model.entity.Department;
import com.ces.exam.model.entity.Role;
import com.ces.exam.model.entity.User;
import com.ces.exam.payload.request.DepartmentRequest;
import com.ces.exam.payload.response.DepartmentDetailResponse;
import com.ces.exam.payload.response.DepartmentResponse;
import com.ces.exam.repository.CategoryRepository;
import com.ces.exam.repository.DepartmentRepository;
import com.ces.exam.repository.ExamSessionRepository;
import com.ces.exam.repository.UserRepository;
import com.ces.exam.exception.ResourceNotFoundException;
import com.ces.exam.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExamSessionRepository sessionRepository;

    public DepartmentService(DepartmentRepository departmentRepository, UserRepository userRepository,
                             CategoryRepository categoryRepository, ExamSessionRepository sessionRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.sessionRepository = sessionRepository;
    }

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /** Paginated variant (opt-in via ?page=). */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<DepartmentResponse> getAllDepartments(org.springframework.data.domain.Pageable pageable) {
        return departmentRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public DepartmentDetailResponse getDepartmentDetail(Long id) {
        Department dep = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        List<User> members = userRepository.findByDepartmentIdOrderByFirstNameAsc(id);

        // map userId -> [userId, completedCount, avgScore, lastEndTime, passedCount]
        Map<Long, Object[]> statsByUser = new HashMap<>();
        for (Object[] r : sessionRepository.memberExamStatsForDepartment(id)) {
            statsByUser.put(((Number) r[0]).longValue(), r);
        }

        long totalCompleted = 0;
        long totalPassed = 0;
        List<DepartmentDetailResponse.Member> memberDtos = new ArrayList<>();
        for (User u : members) {
            Object[] r = statsByUser.get(u.getId());
            long completed = r != null ? ((Number) r[1]).longValue() : 0;
            Integer avg = r != null && r[2] != null ? (int) Math.round(((Number) r[2]).doubleValue()) : null;
            LocalDateTime last = r != null ? (LocalDateTime) r[3] : null;
            long passed = r != null && r[4] != null ? ((Number) r[4]).longValue() : 0;
            totalCompleted += completed;
            totalPassed += passed;

            List<String> roles = u.getRoles().stream()
                    .map(Role::getName)
                    .map(DepartmentService::friendlyRole)
                    .collect(Collectors.toList());

            memberDtos.add(new DepartmentDetailResponse.Member(
                    u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(),
                    u.getStatus(), roles, completed, avg, last));
        }

        // most-active members first, then strongest, then alphabetical
        memberDtos.sort(Comparator
                .comparingLong(DepartmentDetailResponse.Member::getCompletedExams).reversed()
                .thenComparing(m -> m.getAvgScore() == null ? -1 : m.getAvgScore(), Comparator.reverseOrder())
                .thenComparing(DepartmentDetailResponse.Member::getFirstName, Comparator.nullsLast(String::compareToIgnoreCase)));

        Double deptAvgD = sessionRepository.avgScoreForDepartment(id);
        Integer deptAvg = deptAvgD != null ? (int) Math.round(deptAvgD) : null;
        Integer passRate = totalCompleted > 0 ? (int) Math.round(totalPassed * 100.0 / totalCompleted) : null;

        return new DepartmentDetailResponse(dep.getId(), dep.getName(), dep.getCreatedAt(),
                members.size(), totalCompleted, deptAvg, passRate, memberDtos);
    }

    public DepartmentResponse createDepartment(DepartmentRequest request) {
        Department department = new Department();
        department.setName(request.getName());
        return mapToResponse(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        department.setName(request.getName());
        return mapToResponse(departmentRepository.save(department));
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        if (userRepository.existsByDepartmentId(id)) {
            throw new ValidationException("Bu şöbədə istifadəçilər var. Əvvəlcə onları başqa şöbəyə köçürün.");
        }
        if (categoryRepository.existsByDepartmentId(id)) {
            throw new ValidationException("Bu şöbənin sual bankı kateqoriyaları var. Əvvəlcə onları silin.");
        }
        departmentRepository.delete(department);
    }

    private DepartmentResponse mapToResponse(Department dep) {
        long memberCount = userRepository.countByDepartmentId(dep.getId());
        return new DepartmentResponse(dep.getId(), dep.getName(), dep.getCreatedAt(), memberCount);
    }

    private static String friendlyRole(String roleName) {
        String key = roleName == null ? "" : roleName.replaceFirst("^ROLE_", "");
        switch (key) {
            case "ADMIN": return "Administrator";
            case "EMPLOYEE": return "İşçi";
            case "CANDIDATE": return "Namizəd";
            default: return key;
        }
    }
}
