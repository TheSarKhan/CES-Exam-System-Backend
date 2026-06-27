package com.ces.exam.service;

import com.ces.exam.model.entity.Category;
import com.ces.exam.model.entity.Department;
import com.ces.exam.model.entity.Question;
import com.ces.exam.model.entity.QuestionOption;
import com.ces.exam.model.entity.Topic;
import com.ces.exam.model.enums.Difficulty;
import com.ces.exam.payload.request.CategoryRequest;
import com.ces.exam.payload.request.QuestionRequest;
import com.ces.exam.payload.request.TopicRequest;
import com.ces.exam.payload.response.CategoryResponse;
import com.ces.exam.payload.response.QuestionOptionResponse;
import com.ces.exam.payload.response.QuestionResponse;
import com.ces.exam.payload.response.TopicResponse;
import com.ces.exam.repository.CategoryRepository;
import com.ces.exam.repository.DepartmentRepository;
import com.ces.exam.repository.ExamSessionQuestionRepository;
import com.ces.exam.repository.QuestionRepository;
import com.ces.exam.repository.TopicRepository;
import com.ces.exam.exception.ResourceNotFoundException;
import com.ces.exam.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionBankService {

    private final CategoryRepository categoryRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;
    private final DepartmentRepository departmentRepository;
    private final ExamSessionQuestionRepository examSessionQuestionRepository;

    public QuestionBankService(CategoryRepository categoryRepository, TopicRepository topicRepository,
                               QuestionRepository questionRepository, DepartmentRepository departmentRepository,
                               ExamSessionQuestionRepository examSessionQuestionRepository) {
        this.categoryRepository = categoryRepository;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
        this.departmentRepository = departmentRepository;
        this.examSessionQuestionRepository = examSessionQuestionRepository;
    }

    // ---------- CATEGORY ----------

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(Long departmentId) {
        List<Category> categories = departmentId != null
                ? categoryRepository.findByDepartmentIdOrderByNameAsc(departmentId)
                : categoryRepository.findAll();
        return categories.stream().map(this::mapToCategoryResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return mapToCategoryResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        Category category = new Category();
        category.setDepartment(department);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return mapToCategoryResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (request.getDepartmentId() != null
                && !request.getDepartmentId().equals(category.getDepartment().getId())) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            category.setDepartment(department);
        }
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return mapToCategoryResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (topicRepository.existsByCategoryId(id)) {
            throw new ValidationException("Bu kateqoriyada mövzular var. Əvvəlcə mövzuları silin.");
        }
        categoryRepository.delete(category);
    }

    private CategoryResponse mapToCategoryResponse(Category c) {
        Department d = c.getDepartment();
        long topicCount = topicRepository.countByCategoryId(c.getId());
        long questionCount = questionRepository.countByTopic_Category_Id(c.getId());
        return new CategoryResponse(c.getId(),
                d != null ? d.getId() : null,
                d != null ? d.getName() : null,
                c.getName(), c.getDescription(),
                topicCount, questionCount);
    }

    // ---------- TOPIC ----------

    @Transactional(readOnly = true)
    public List<TopicResponse> getTopicsByCategory(Long categoryId) {
        return topicRepository.findByCategoryIdOrderByNameAsc(categoryId).stream()
                .map(t -> new TopicResponse(t.getId(), t.getCategory().getId(), t.getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public TopicResponse createTopic(TopicRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Topic topic = new Topic();
        topic.setCategory(category);
        topic.setName(request.getName());
        Topic saved = topicRepository.save(topic);
        return new TopicResponse(saved.getId(), category.getId(), saved.getName());
    }

    @Transactional
    public TopicResponse updateTopic(Long id, TopicRequest request) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
        topic.setName(request.getName());
        Topic saved = topicRepository.save(topic);
        return new TopicResponse(saved.getId(), saved.getCategory().getId(), saved.getName());
    }

    @Transactional
    public void deleteTopic(Long id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
        if (questionRepository.existsByTopicId(id)) {
            throw new ValidationException("Bu mövzuda suallar var. Əvvəlcə sualları silin.");
        }
        topicRepository.delete(topic);
    }

    // ---------- QUESTION ----------

    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByTopic(Long topicId) {
        return questionRepository.findByTopicIdOrderByIdDesc(topicId).stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found");
        }
        return questionRepository.findByTopic_Category_IdOrderByIdDesc(categoryId).stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuestionResponse getQuestion(Long id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        return mapToQuestionResponse(q);
    }

    @Transactional
    public QuestionResponse createQuestion(QuestionRequest request) {
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        Question question = new Question();
        question.setTopic(topic);
        applyQuestionFields(question, request);
        question.setOptions(buildOptions(request));

        return mapToQuestionResponse(questionRepository.save(question));
    }

    @Transactional
    public com.ces.exam.payload.response.BulkImportResponse bulkCreate(
            com.ces.exam.payload.request.BulkQuestionRequest request) {
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new ValidationException("İdxal üçün sual yoxdur.");
        }

        List<com.ces.exam.payload.response.BulkImportResponse.RowError> errors = new ArrayList<>();
        List<Question> toCreate = new ArrayList<>();
        int row = 0;
        for (QuestionRequest q : request.getQuestions()) {
            row++;
            String err = validateBulkRow(q);
            if (err != null) {
                errors.add(new com.ces.exam.payload.response.BulkImportResponse.RowError(row, err));
                continue;
            }
            Question question = new Question();
            question.setTopic(topic);
            applyQuestionFields(question, q);
            if (question.getScore() == null) question.setScore(java.math.BigDecimal.ONE);
            List<QuestionOption> opts = buildOptions(q);
            if (opts != null) opts.forEach(o -> o.setQuestion(question));
            question.setOptions(opts);
            toCreate.add(question);
        }

        if (!toCreate.isEmpty()) {
            questionRepository.saveAll(toCreate);
        }
        return new com.ces.exam.payload.response.BulkImportResponse(toCreate.size(), errors);
    }

    private String validateBulkRow(QuestionRequest q) {
        com.ces.exam.model.enums.QuestionType t = q.getType();
        if (t == null) return "Sual tipi yoxdur";
        boolean supported = t == com.ces.exam.model.enums.QuestionType.SINGLE_CHOICE
                || t == com.ces.exam.model.enums.QuestionType.MULTIPLE_CHOICE
                || t == com.ces.exam.model.enums.QuestionType.TRUE_FALSE
                || t == com.ces.exam.model.enums.QuestionType.SHORT_TEXT
                || t == com.ces.exam.model.enums.QuestionType.LONG_TEXT;
        if (!supported) return "Dəstəklənməyən tip: " + t.name();
        if (q.getText() == null || q.getText().isBlank()) return "Sual mətni yoxdur";

        boolean choice = t == com.ces.exam.model.enums.QuestionType.SINGLE_CHOICE
                || t == com.ces.exam.model.enums.QuestionType.MULTIPLE_CHOICE
                || t == com.ces.exam.model.enums.QuestionType.TRUE_FALSE;
        if (choice) {
            if (q.getOptions() == null || q.getOptions().size() < 2) return "Ən azı 2 variant lazımdır";
            long correct = q.getOptions().stream()
                    .filter(o -> Boolean.TRUE.equals(o.getIsCorrect())).count();
            if (correct < 1) return "Düzgün variant qeyd olunmayıb";
            if (t == com.ces.exam.model.enums.QuestionType.SINGLE_CHOICE && correct != 1)
                return "Tək seçimdə dəqiq 1 düzgün variant olmalıdır";
            if (t == com.ces.exam.model.enums.QuestionType.TRUE_FALSE && q.getOptions().size() != 2)
                return "Doğru/Yanlış üçün dəqiq 2 variant olmalıdır";
        }
        return null;
    }

    @Transactional
    public QuestionResponse updateQuestion(Long id, QuestionRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        if (request.getTopicId() != null && !request.getTopicId().equals(question.getTopic().getId())) {
            Topic topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
            question.setTopic(topic);
        }
        applyQuestionFields(question, request);

        // Replace options in place to respect orphanRemoval.
        List<QuestionOption> newOptions = buildOptions(request);
        if (question.getOptions() == null) {
            question.setOptions(newOptions);
        } else {
            question.getOptions().clear();
            if (newOptions != null) {
                for (QuestionOption opt : newOptions) {
                    opt.setQuestion(question);
                    question.getOptions().add(opt);
                }
            }
        }

        return mapToQuestionResponse(questionRepository.save(question));
    }

    @Transactional
    public void deleteQuestion(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        if (examSessionQuestionRepository.existsByQuestionId(id)) {
            throw new ValidationException(
                    "Bu sual imtahan sessiyalarında istifadə olunub — silmək əvəzinə deaktiv edin.");
        }
        questionRepository.delete(question);
    }

    private void applyQuestionFields(Question question, QuestionRequest request) {
        question.setType(request.getType());
        question.setText(request.getText());
        question.setImageUrl(request.getImageUrl());
        question.setScore(request.getScore());
        question.setDifficulty(request.getDifficulty() != null ? request.getDifficulty() : Difficulty.MEDIUM);
    }

    private List<QuestionOption> buildOptions(QuestionRequest request) {
        if (request.getOptions() == null) {
            return null;
        }
        return request.getOptions().stream().map(optReq -> {
            QuestionOption opt = new QuestionOption();
            opt.setText(optReq.getText());
            opt.setImageUrl(optReq.getImageUrl());
            opt.setCorrect(optReq.getIsCorrect());
            opt.setSortOrder(optReq.getSortOrder());
            return opt;
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    private QuestionResponse mapToQuestionResponse(Question q) {
        List<QuestionOptionResponse> options = null;
        if (q.getOptions() != null) {
            options = q.getOptions().stream()
                    .map(o -> new QuestionOptionResponse(o.getId(), o.getText(), o.getImageUrl(), o.getCorrect(), o.getSortOrder()))
                    .collect(Collectors.toList());
        }
        return new QuestionResponse(q.getId(), q.getTopic() != null ? q.getTopic().getId() : null,
                q.getType().name(), q.getText(), q.getImageUrl(),
                q.getScore(),
                q.getDifficulty() != null ? q.getDifficulty().name() : Difficulty.MEDIUM.name(),
                q.getActive(), options);
    }
}
