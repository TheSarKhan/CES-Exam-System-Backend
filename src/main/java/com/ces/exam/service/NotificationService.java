package com.ces.exam.service;

import com.ces.exam.model.entity.ExamSession;
import com.ces.exam.model.entity.NotificationRead;
import com.ces.exam.model.enums.SessionStatus;
import com.ces.exam.payload.response.NotificationFeedResponse;
import com.ces.exam.repository.ExamSessionQuestionRepository;
import com.ces.exam.repository.ExamSessionRepository;
import com.ces.exam.repository.NotificationReadRepository;
import com.ces.exam.repository.SessionViolationRepository;
import com.ces.exam.security.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    private final ExamSessionRepository sessionRepository;
    private final ExamSessionQuestionRepository sessionQuestionRepository;
    private final SessionViolationRepository violationRepository;
    private final NotificationReadRepository readRepository;

    public NotificationService(ExamSessionRepository sessionRepository,
                               ExamSessionQuestionRepository sessionQuestionRepository,
                               SessionViolationRepository violationRepository,
                               NotificationReadRepository readRepository) {
        this.sessionRepository = sessionRepository;
        this.sessionQuestionRepository = sessionQuestionRepository;
        this.violationRepository = violationRepository;
        this.readRepository = readRepository;
    }

    /**
     * Admin notification feed derived from recent completed sessions. Each completion is one
     * notification, enriched with its pending-grading and anti-cheat counts; "unread" compares
     * the completion time against the admin's last-cleared watermark.
     */
    @Transactional(readOnly = true)
    public NotificationFeedResponse getFeed() {
        LocalDateTime lastRead = readRepository.findById(currentUserId())
                .map(NotificationRead::getLastReadAt)
                .orElse(null);

        List<ExamSession> recent = sessionRepository.findTop20ByStatusOrderByEndTimeDesc(SessionStatus.COMPLETED);
        List<NotificationFeedResponse.Item> items = new ArrayList<>();
        for (ExamSession s : recent) {
            int pending = (int) sessionQuestionRepository.countPendingGradingForSession(s.getId());
            int violations = (int) violationRepository.countBySessionId(s.getId());
            String type = pending > 0 ? "GRADING" : (violations > 0 ? "VIOLATION" : "RESULT");
            boolean unread = lastRead == null
                    || (s.getEndTime() != null && s.getEndTime().isAfter(lastRead));
            String userName = (s.getUser().getFirstName() + " " + s.getUser().getLastName()).trim();
            items.add(new NotificationFeedResponse.Item(
                    s.getId(),
                    s.getAssignment().getExam().getId(),
                    s.getAssignment().getExam().getTitle(),
                    userName,
                    s.getScore(),
                    s.getPassed(),
                    pending,
                    violations,
                    type,
                    s.getEndTime(),
                    unread));
        }

        long unreadCount = lastRead == null
                ? sessionRepository.countByStatus(SessionStatus.COMPLETED)
                : sessionRepository.countByStatusAndEndTimeAfter(SessionStatus.COMPLETED, lastRead);

        return new NotificationFeedResponse((int) unreadCount, items);
    }

    @Transactional
    public void markAllRead() {
        Long userId = currentUserId();
        NotificationRead state = readRepository.findById(userId).orElseGet(() -> {
            NotificationRead nr = new NotificationRead();
            nr.setUserId(userId);
            return nr;
        });
        state.setLastReadAt(LocalDateTime.now());
        readRepository.save(state);
    }

    private Long currentUserId() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userDetails.getUser().getId();
    }
}
