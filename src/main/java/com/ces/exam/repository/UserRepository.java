package com.ces.exam.repository;

import com.ces.exam.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    long countByStatus(String status);

    long countByDepartmentId(Long departmentId);

    boolean existsByDepartmentId(Long departmentId);

    List<User> findByDepartmentIdOrderByFirstNameAsc(Long departmentId);
}
