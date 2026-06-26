package com.ces.exam.repository;

import com.ces.exam.model.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByCategoryId(Long categoryId);

    List<Topic> findByCategoryIdOrderByNameAsc(Long categoryId);

    boolean existsByCategoryId(Long categoryId);

    long countByCategoryId(Long categoryId);
}
