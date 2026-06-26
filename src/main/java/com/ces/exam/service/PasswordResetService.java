package com.ces.exam.service;

import com.ces.exam.exception.ValidationException;
import com.ces.exam.model.entity.PasswordResetToken;
import com.ces.exam.model.entity.User;
import com.ces.exam.repository.PasswordResetTokenRepository;
import com.ces.exam.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final int EXPIRY_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Creates a reset token and e-mails the link. Never reveals whether the e-mail
     * exists — the caller always returns 200 regardless of the outcome.
     */
    @Transactional
    public void requestReset(String email) {
        if (email == null || email.isBlank()) return;
        Optional<User> userOpt = userRepository.findByEmail(email.trim());
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();

        String token = UUID.randomUUID().toString().replace("-", "")
                + Long.toHexString(System.nanoTime());

        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(token);
        prt.setUser(user);
        prt.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
        tokenRepository.save(prt);

        String name = (user.getFirstName() + " " + user.getLastName()).trim();
        emailService.trySendPasswordReset(user.getEmail(), name, token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = tokenRepository.findByToken(token)
                .filter(PasswordResetToken::isUsable)
                .orElseThrow(() -> new ValidationException("Link etibarsızdır və ya vaxtı bitib. Yenidən sorğu göndərin."));

        User user = prt.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        prt.setUsedAt(LocalDateTime.now());
        tokenRepository.save(prt);
    }
}
