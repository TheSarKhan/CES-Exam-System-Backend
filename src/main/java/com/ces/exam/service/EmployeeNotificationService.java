package com.ces.exam.service;

import com.ces.exam.exception.ResourceNotFoundException;
import com.ces.exam.model.entity.Exam;
import com.ces.exam.model.entity.ExamAssignment;
import com.ces.exam.model.entity.ExamSession;
import com.ces.exam.model.entity.NotificationRead;
import com.ces.exam.model.entity.User;
import com.ces.exam.model.enums.SessionStatus;
import com.ces.exam.payload.response.EmployeeNotificationResponse;
import com.ces.exam.payload.response.EmployeeNotificationResponse.Item;
import com.ces.exam.repository.ExamAssignmentRepository;
import com.ces.exam.repository.ExamSessionRepository;
import com.ces.exam.repository.NotificationReadRepository;
import com.ces.exam.repository.UserRepository;
import com.ces.exam.security.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmployeeNotificationService {

    private final ExamAssignmentRepository assignmentRepository;
    private final ExamSessionRepository sessionRepository;
    private final NotificationReadRepository readRepository;
    private final UserRepository userRepository;

    public EmployeeNotificationService(ExamAssignmentRepository assignmentRepository,
                                       ExamSessionRepository sessionRepository,
                                       NotificationReadRepository readRepository,
                                       UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.sessionRepository = sessionRepository;
        this.readRepository = readRepository;
        this.userRepository = userRepository;
    }

    /**
     * Personal notification feed derived from the employee's assignments:
     * completed → "result ready", pending with a near deadline → "deadline", otherwise "assigned".
     */
    @Transactional(readOnly = true)
    public EmployeeNotificationResponse getFeed() {
        User user = currentUser();
        Long deptId = user.getDepartment() != null ? user.getDepartment().getId() : null;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastRead = readRepository.findById(user.getId())
                .map(NotificationRead::getLastReadAt).orElse(null);

        List<ExamAssignment> assignments = assignmentRepository.findMyAssignments(user.getId(), deptId);
        if (assignments.isEmpty()) return new EmployeeNotificationResponse(0, List.of());

        List<Long> ids = assignments.stream().map(ExamAssignment::getId).collect(Collectors.toList());
        Map<Long, ExamSession> byAssignment = sessionRepository
                .findByUserIdAndAssignmentIdIn(user.getId(), ids).stream()
                .collect(Collectors.toMap(s -> s.getAssignment().getId(), s -> s,
                        (a, b) -> a.getStartTime().isAfter(b.getStartTime()) ? a : b));

        List<Item> items = new ArrayList<>();
        for (ExamAssignment a : assignments) {
            Exam exam = a.getExam();
            ExamSession session = byAssignment.get(a.getId());
            if (session != null && session.getStatus() == SessionStatus.COMPLETED) {
                LocalDateTime t = session.getEndTime() != null ? session.getEndTime() : a.getCreatedAt();
                items.add(new Item(exam.getId(), session.getId(), a.getId(), exam.getTitle(),
                        "RESULT", session.getScore(), session.getPassed(), null, t, isUnread(t, lastRead)));
            } else {
                boolean deadlineSoon = a.getEndDate() != null
                        && a.getEndDate().isAfter(now) && a.getEndDate().isBefore(now.plusDays(3));
                String type = deadlineSoon ? "DEADLINE" : "ASSIGNED";
                LocalDateTime t = a.getCreatedAt() != null ? a.getCreatedAt() : now;
                items.add(new Item(exam.getId(), session != null ? session.getId() : null, a.getId(),
                        exam.getTitle(), type, null, null, a.getEndDate(), t, isUnread(t, lastRead)));
            }
        }

        items.sort(Comparator.comparing(Item::getTime,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        List<Item> top = items.size() > 25 ? new ArrayList<>(items.subList(0, 25)) : items;
        int unread = (int) top.stream().filter(Item::isUnread).count();
        return new EmployeeNotificationResponse(unread, top);
    }

    @Transactional
    public void markAllRead() {
        Long userId = currentUser().getId();
        NotificationRead state = readRepository.findById(userId).orElseGet(() -> {
            NotificationRead nr = new NotificationRead();
            nr.setUserId(userId);
            return nr;
        });
        state.setLastReadAt(LocalDateTime.now());
        readRepository.save(state);
    }

    private boolean isUnread(LocalDateTime t, LocalDateTime lastRead) {
        return lastRead == null || (t != null && t.isAfter(lastRead));
    }

    private User currentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("İstifadəçi tapılmadı"));
    }
}
