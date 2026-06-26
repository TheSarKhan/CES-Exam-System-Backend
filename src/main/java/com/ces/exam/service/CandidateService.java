package com.ces.exam.service;

import com.ces.exam.exception.ResourceNotFoundException;
import com.ces.exam.model.entity.Role;
import com.ces.exam.model.entity.User;
import com.ces.exam.repository.RoleRepository;
import com.ces.exam.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Creates lightweight candidate accounts that own link-based exam sessions. */
@Service
public class CandidateService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public CandidateService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Creates a CANDIDATE user. A blank name falls back to "Namizəd". */
    public User create(String fullName) {
        String name = (fullName == null || fullName.isBlank()) ? "Namizəd" : fullName.trim();
        String[] parts = name.split("\\s+", 2);

        User candidate = new User();
        candidate.setFirstName(parts[0]);
        candidate.setLastName(parts.length > 1 ? parts[1] : "");
        candidate.setEmail("candidate." + UUID.randomUUID() + "@link.local");
        candidate.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        candidate.setStatus("ACTIVE");

        Role candidateRole = roleRepository.findByName("ROLE_CANDIDATE")
                .orElseThrow(() -> new ResourceNotFoundException("Candidate role not configured"));
        Set<Role> roles = new HashSet<>();
        roles.add(candidateRole);
        candidate.setRoles(roles);

        return userRepository.save(candidate);
    }
}
