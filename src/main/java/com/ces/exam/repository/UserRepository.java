package com.ces.exam.repository;

import com.ces.exam.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    long countByStatus(String status);

    long countByDepartmentId(Long departmentId);

    boolean existsByDepartmentId(Long departmentId);

    List<User> findByDepartmentIdOrderByFirstNameAsc(Long departmentId);

    /**
     * Server-side search + filter + paginate for the admin users list.
     * roleFilter: ALL | PLATFORM (admin or employee) | ADMIN | EMPLOYEE | CANDIDATE
     * (candidate-only = has CANDIDATE but neither ADMIN nor EMPLOYEE).
     * search/status/deptId are ignored when null.
     */
    @Query("SELECT u FROM User u LEFT JOIN u.department d WHERE " +
           "(:search IS NULL OR LOWER(u.firstName) LIKE :search OR LOWER(u.lastName) LIKE :search " +
           "   OR LOWER(u.email) LIKE :search) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:deptId IS NULL OR d.id = :deptId) AND " +
           "( :roleFilter = 'ALL' " +
           "  OR (:roleFilter = 'ADMIN' AND EXISTS (SELECT 1 FROM u.roles r WHERE r.name = 'ROLE_ADMIN')) " +
           "  OR (:roleFilter = 'EMPLOYEE' AND EXISTS (SELECT 1 FROM u.roles r WHERE r.name = 'ROLE_EMPLOYEE')) " +
           "  OR (:roleFilter = 'PLATFORM' AND EXISTS (SELECT 1 FROM u.roles r WHERE r.name IN ('ROLE_ADMIN','ROLE_EMPLOYEE'))) " +
           "  OR (:roleFilter = 'CANDIDATE' AND EXISTS (SELECT 1 FROM u.roles r WHERE r.name = 'ROLE_CANDIDATE') " +
           "      AND NOT EXISTS (SELECT 1 FROM u.roles r2 WHERE r2.name IN ('ROLE_ADMIN','ROLE_EMPLOYEE'))) )")
    Page<User> search(@Param("search") String search,
                      @Param("status") String status,
                      @Param("deptId") Long deptId,
                      @Param("roleFilter") String roleFilter,
                      Pageable pageable);
}
