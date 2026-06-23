package com.ces.exam.service;

import com.ces.exam.model.entity.Category;
import com.ces.exam.model.entity.Question;
import com.ces.exam.model.entity.QuestionOption;
import com.ces.exam.model.entity.Topic;
import com.ces.exam.payload.request.CategoryRequest;
import com.ces.exam.payload.request.QuestionRequest;
import com.ces.exam.payload.request.TopicRequest;
import com.ces.exam.payload.response.CategoryResponse;
import com.ces.exam.payload.response.QuestionOptionResponse;
import com.ces.exam.payload.response.QuestionResponse;
import com.ces.exam.payload.response.TopicResponse;
import com.ces.exam.repository.CategoryRepository;
import com.ces.exam.repository.QuestionRepository;
import com.ces.exam.repository.TopicRepository;
import com.ces.exam.exception.ResourceNotFoundException;
import com.ces.exam.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionBankService {

    private final CategoryRepository categoryRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;

    public QuestionBankService(CategoryRepository categoryRepository, TopicRepository topicRepository, QuestionRepository questionRepository) {
        this.categoryRepository = categoryRepository;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
    }

    // CATEGORY
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getDescription()))
                .collect(Collectors.toList());
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        Category saved = categoryRepository.save(category);
        return new CategoryResponse(saved.getId(), saved.getName(), saved.getDescription());
    }

    // TOPIC
    public List<TopicResponse> getTopicsByCategory(Long categoryId) {
        return topicRepository.findByCategoryId(categoryId).stream()
                .map(t -> new TopicResponse(t.getId(), t.getCategory().getId(), t.getName()))
                .collect(Collectors.toList());
    }

    public TopicResponse createTopic(TopicRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Topic topic = new Topic();
        topic.setCategory(category);
        topic.setName(request.getName());
        Topic saved = topicRepository.save(topic);
        return new TopicResponse(saved.getId(), category.getId(), saved.getName());
    }

    // QUESTION
    public List<QuestionResponse> getQuestionsByTopic(Long topicId) {
        return questionRepository.findByTopicId(topicId).stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public QuestionResponse createQuestion(QuestionRequest request) {
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        Question question = new Question();
        question.setTopic(topic);
        question.setType(request.getType());
        question.setText(request.getText());
        question.setScore(request.getScore());

        if (request.getOptions() != null) {
            List<QuestionOption> options = request.getOptions().stream().map(optReq -> {
                QuestionOption opt = new QuestionOption();
                opt.setText(optReq.getText());
                opt.setCorrect(optReq.getIsCorrect());
                opt.setSortOrder(optReq.getSortOrder());
                return opt;
            }).collect(Collectors.toList());
            question.setOptions(options);
        }

        Question saved = questionRepository.save(question);
        return mapToQuestionResponse(saved);
    }

    private QuestionResponse mapToQuestionResponse(Question q) {
        List<QuestionOptionResponse> options = null;
        if (q.getOptions() != null) {
            options = q.getOptions().stream()
                    .map(o -> new QuestionOptionResponse(o.getId(), o.getText(), o.getCorrect(), o.getSortOrder()))
                    .collect(Collectors.toList());
        }
        return new QuestionResponse(q.getId(), q.getTopic().getId(), q.getType().name(), q.getText(), q.getScore(), q.getActive(), options);
    }
}
