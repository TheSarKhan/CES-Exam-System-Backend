package com.ces.exam.repository;

import com.ces.exam.model.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTopicId(Long topicId);

    List<Question> findByTopicIdAndIsActiveTrue(Long topicId);
}
