package com.ces.exam.service;

import com.ces.exam.model.entity.*;
import com.ces.exam.payload.request.ExamAssignmentRequest;
import com.ces.exam.payload.request.ExamRequest;
import com.ces.exam.payload.response.ExamAssignmentResponse;
import com.ces.exam.payload.response.ExamResponse;
import com.ces.exam.repository.*;
import com.ces.exam.exception.ResourceNotFoundException;
import com.ces.exam.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExamService {
    private final ExamRepository examRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ExamAssignmentRepository examAssignmentRepository;

    public ExamService(ExamRepository examRepository, TopicRepository topicRepository, UserRepository userRepository, DepartmentRepository departmentRepository, ExamAssignmentRepository examAssignmentRepository) {
        this.examRepository = examRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.examAssignmentRepository = examAssignmentRepository;
    }

    @Transactional
    public ExamResponse createExam(ExamRequest request) {
        Exam exam = new Exam();
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setType(request.getType());
        exam.setPassMark(request.getPassMark());
        exam.setDurationMinutes(request.getDurationMinutes());

        if (request.getTopicConfigs() != null) {
            List<ExamTopicConfig> configs = request.getTopicConfigs().stream().map(req -> {
                Topic topic = topicRepository.findById(req.getTopicId())
                        .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
                ExamTopicConfig config = new ExamTopicConfig();
                config.setTopic(topic);
                config.setQuestionCount(req.getQuestionCount());
                return config;
            }).collect(Collectors.toList());
            exam.setTopicConfigs(configs);
        }

        Exam saved = examRepository.save(exam);
        return mapToResponse(saved);
    }

    public List<ExamResponse> getAllExams() {
        return examRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public ExamAssignmentResponse assignExam(ExamAssignmentRequest request) {
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        ExamAssignment assignment = new ExamAssignment();
        assignment.setExam(exam);
        assignment.setStartDate(request.getStartDate());
        assignment.setEndDate(request.getEndDate());

        User assignedUser = null;
        if (request.getUserId() != null) {
            assignedUser = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            assignment.setAssignedUser(assignedUser);
            assignment.setAccessToken(UUID.randomUUID().toString());
        } else if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            assignment.setAssignedDepartment(dept);
        } else {
            throw new ValidationException("Either UserId or DepartmentId must be provided");
        }

        ExamAssignment saved = examAssignmentRepository.save(assignment);
        String candidateName = assignedUser != null
                ? assignedUser.getFirstName() + " " + assignedUser.getLastName()
                : null;
        return new ExamAssignmentResponse(
                saved.getId(),
                saved.getAccessToken(),
                candidateName,
                exam.getTitle()
        );
    }

    private ExamResponse mapToResponse(Exam exam) {
        List<ExamResponse.ExamTopicConfigResponse> topicConfigs = null;
        if (exam.getTopicConfigs() != null) {
            topicConfigs = exam.getTopicConfigs().stream()
                    .map(tc -> new ExamResponse.ExamTopicConfigResponse(tc.getTopic().getId(), tc.getTopic().getName(), tc.getQuestionCount()))
                    .collect(Collectors.toList());
        }
        return new ExamResponse(exam.getId(), exam.getTitle(), exam.getType().name(), exam.getPassMark(), exam.getDurationMinutes(), topicConfigs);
    }
}
