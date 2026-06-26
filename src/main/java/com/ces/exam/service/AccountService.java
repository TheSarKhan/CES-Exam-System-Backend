package com.ces.exam.service;

import com.ces.exam.exception.ResourceNotFoundException;
import com.ces.exam.exception.ValidationException;
import com.ces.exam.model.entity.ExamSession;
import com.ces.exam.model.entity.Role;
import com.ces.exam.model.entity.User;
import com.ces.exam.payload.request.ChangePasswordRequest;
import com.ces.exam.payload.request.UpdateProfileRequest;
import com.ces.exam.payload.response.AccountResponse;
import com.ces.exam.payload.response.MyAssignmentResponse;
import com.ces.exam.payload.response.ProgressResponse;
import com.ces.exam.repository.ExamSessionQuestionRepository;
import com.ces.exam.repository.ExamSessionRepository;
import com.ces.exam.repository.UserRepository;
import com.ces.exam.security.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExamSessionService examSessionService;
    private final ExamSessionRepository sessionRepository;
    private final ExamSessionQuestionRepository sessionQuestionRepository;

    public AccountService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          ExamSessionService examSessionService,
                          ExamSessionRepository sessionRepository,
                          ExamSessionQuestionRepository sessionQuestionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.examSessionService = examSessionService;
        this.sessionRepository = sessionRepository;
        this.sessionQuestionRepository = sessionQuestionRepository;
    }

    @Transactional(readOnly = true)
    public AccountResponse getMyProfile() {
        return buildProfile(currentUser());
    }

    @Transactional
    public AccountResponse updateProfile(UpdateProfileRequest request) {
        User user = currentUser();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        userRepository.save(user);
        return buildProfile(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ValidationException("Cari parol yanlışdır");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new ValidationException("Yeni parol cari paroldan fərqli olmalıdır");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public ProgressResponse getProgress() {
        User user = currentUser();

        List<ExamSession> sessions = sessionRepository.findCompletedExamSessionsForUser(user.getId());
        List<ProgressResponse.TrendPoint> trend = sessions.stream()
                .map(s -> new ProgressResponse.TrendPoint(
                        s.getAssignment().getExam().getTitle(), s.getScore(), s.getPassed(), s.getEndTime()))
                .collect(Collectors.toList());

        int completed = sessions.size();
        int passed = (int) sessions.stream().filter(s -> Boolean.TRUE.equals(s.getPassed())).count();
        Integer best = sessions.stream()
                .map(ExamSession::getScore).filter(Objects::nonNull)
                .map(bd -> bd.setScale(0, RoundingMode.HALF_UP).intValue())
                .max(Integer::compareTo).orElse(null);

        Double avgD = sessionRepository.avgScoreForUser(user.getId());
        Integer avg = avgD != null ? (int) Math.round(avgD) : null;

        Integer deptAvg = null;
        String deptName = null;
        if (user.getDepartment() != null) {
            deptName = user.getDepartment().getName();
            Double d = sessionRepository.avgScoreForDepartment(user.getDepartment().getId());
            deptAvg = d != null ? (int) Math.round(d) : null;
        }

        List<ProgressResponse.CategoryStat> categories = new ArrayList<>();
        for (Object[] r : sessionQuestionRepository.categoryStatsForUser(user.getId())) {
            String name = (String) r[0];
            long correct = ((Number) r[1]).longValue();
            long graded = ((Number) r[2]).longValue();
            Integer rate = graded > 0 ? (int) Math.round(correct * 100.0 / graded) : null;
            categories.add(new ProgressResponse.CategoryStat(name, correct, graded, rate));
        }

        return new ProgressResponse(completed, avg, best, passed, deptAvg, deptName, trend, categories);
    }

    private AccountResponse buildProfile(User user) {
        // getMyAssignments() reads from the same security context (current user) and
        // already merges sessions, so we can derive personal stats straight from it.
        List<MyAssignmentResponse> assignments = examSessionService.getMyAssignments();

        int assigned = assignments.size();
        int completed = 0, pending = 0, passed = 0, scoredCount = 0;
        BigDecimal scoreSum = BigDecimal.ZERO;
        Integer best = null;

        for (MyAssignmentResponse a : assignments) {
            if ("COMPLETED".equals(a.getStatus())) {
                completed++;
                // passed != null marks a fully-graded real exam (surveys & pending grading stay null).
                if (a.getPassed() != null && a.getScore() != null) {
                    scoredCount++;
                    scoreSum = scoreSum.add(a.getScore());
                    int s = a.getScore().setScale(0, RoundingMode.HALF_UP).intValue();
                    if (best == null || s > best) best = s;
                    if (Boolean.TRUE.equals(a.getPassed())) passed++;
                }
            } else {
                pending++;
            }
        }

        Integer avg = scoredCount > 0
                ? scoreSum.divide(BigDecimal.valueOf(scoredCount), 0, RoundingMode.HALF_UP).intValue()
                : null;

        AccountResponse.Stats stats = new AccountResponse.Stats(
                assigned, completed, pending, avg, passed, best);

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .map(AccountService::friendlyRole)
                .collect(Collectors.toList());

        String deptName = user.getDepartment() != null ? user.getDepartment().getName() : null;

        return new AccountResponse(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                deptName, roles, user.getCreatedAt(), stats);
    }

    private static String friendlyRole(String roleName) {
        String key = roleName == null ? "" : roleName.replaceFirst("^ROLE_", "");
        switch (key) {
            case "ADMIN": return "Administrator";
            case "EMPLOYEE": return "İşçi";
            case "CANDIDATE": return "Namizəd";
            default: return key;
        }
    }

    private User currentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("İstifadəçi tapılmadı"));
    }
}
