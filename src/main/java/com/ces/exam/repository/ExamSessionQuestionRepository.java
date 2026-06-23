package com.ces.exam.repository;

import com.ces.exam.model.entity.ExamSessionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamSessionQuestionRepository extends JpaRepository<ExamSessionQuestion, Long> {
}
