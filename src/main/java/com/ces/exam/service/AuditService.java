package com.ces.exam.service;

import com.ces.exam.model.entity.AuditLog;
import com.ces.exam.payload.response.AuditLogResponse;
import com.ces.exam.payload.response.PageResponse;
import com.ces.exam.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    /** Persists one audit entry. Runs in its own transaction (called after the request completes). */
    @Transactional
    public void save(AuditLog log) {
        repository.save(log);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getLogs(int page, int size, String module) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> result = (module != null && !module.isBlank())
                ? repository.findByModule(module, pageable)
                : repository.findAll(pageable);
        return PageResponse.from(result, AuditLogResponse::new);
    }
}
