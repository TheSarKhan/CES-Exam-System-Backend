package com.ces.exam.repository;

import com.ces.exam.model.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTopicIdOrderByIdDesc(Long topicId);

    List<Question> findByTopicId(Long topicId);

    List<Question> findByTopicIdAndIsActiveTrue(Long topicId);

    boolean existsByTopicId(Long topicId);

    // All questions in a category (across its topics), newest first.
    List<Question> findByTopic_Category_IdOrderByIdDesc(Long categoryId);

    long countByTopic_Category_Id(Long categoryId);
}
