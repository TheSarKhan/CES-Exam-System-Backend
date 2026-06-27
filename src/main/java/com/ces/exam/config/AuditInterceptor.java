package com.ces.exam.config;

import com.ces.exam.model.entity.AuditLog;
import com.ces.exam.model.entity.Role;
import com.ces.exam.model.entity.User;
import com.ces.exam.security.UserDetailsImpl;
import com.ces.exam.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Records an audit entry for every state-changing request (POST/PUT/PATCH/DELETE)
 * under /api/v1. Runs in afterCompletion so the response status and the resolved
 * security context (set by the JWT filter / login) are both available.
 */
@Component
public class AuditInterceptor implements HandlerInterceptor {

    private final AuditService auditService;

    public AuditInterceptor(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        try {
            String method = request.getMethod();
            String path = request.getRequestURI();
            if (path == null || !path.startsWith("/api/v1/")) return;
            if (path.startsWith("/api/v1/admin/audit")) return; // never audit reading the audit

            // Record mutations, plus reads of sensitive data (results, analytics, reports).
            boolean sensitiveRead = "GET".equals(method) && isSensitiveRead(path);
            if (!isMutation(method) && !sensitiveRead) return;

            AuditLog log = new AuditLog();

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl ud) {
                User u = ud.getUser();
                log.setUserId(u.getId());
                log.setUserName((u.getFirstName() + " " + u.getLastName()).trim());
                log.setUserRole(roleLabel(u));
            } else {
                log.setUserName("Anonim");
            }

            log.setHttpMethod(method);
            log.setPath(path);
            log.setStatusCode(response.getStatus());
            log.setIpAddress(clientIp(request));
            log.setModule(moduleLabel(path));
            log.setAction(actionLabel(method, path));

            auditService.save(log);
        } catch (Exception e) {
            // Auditing must never break the actual request/response.
        }
    }

    private boolean isMutation(String method) {
        return "POST".equals(method) || "PUT".equals(method)
                || "PATCH".equals(method) || "DELETE".equals(method);
    }

    /** Reads worth auditing for compliance: exam results, analytics, reports, proctoring. */
    private boolean isSensitiveRead(String path) {
        String l = path.toLowerCase();
        return l.contains("/results") || l.contains("/result")
                || l.contains("/analytics") || l.contains("/violations")
                || l.startsWith("/api/v1/reports");
    }

    private String roleLabel(User u) {
        Set<String> names = u.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        if (names.contains("ROLE_ADMIN")) return "Administrator";
        if (names.contains("ROLE_EMPLOYEE")) return "İşçi";
        if (names.contains("ROLE_CANDIDATE")) return "Namizəd";
        return "—";
    }

    private String moduleLabel(String path) {
        String p = path.replaceFirst("^/api/v1/", "");
        String[] seg = p.split("/");
        String key = seg.length > 0 ? seg[0] : "";
        if ("admin".equals(key) && seg.length > 1) key = "admin/" + seg[1];
        if ("public".equals(key) && seg.length > 1) key = "public/" + seg[1];
        switch (key) {
            case "users": return "İstifadəçilər";
            case "departments": return "Şöbələr";
            case "exams": return "İmtahanlar";
            case "question-bank": return "Sual bankı";
            case "reports": return "Hesabatlar";
            case "auth": return "Autentifikasiya";
            case "account": return "Hesab";
            case "roles": return "Rollar";
            case "assignments": return "Təyinatlar";
            case "exam-sessions":
            case "sessions": return "İmtahan sessiyaları";
            case "admin/settings": return "Parametrlər";
            case "admin/notifications": return "Bildirişlər";
            case "admin/dashboard": return "İdarə paneli";
            case "public/exam-token": return "Namizəd imtahanı";
            default: return seg.length > 0 ? seg[0] : "Digər";
        }
    }

    private String actionLabel(String method, String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith("/auth/login")) return "Giriş";
        if (lower.endsWith("/auth/logout")) return "Çıxış";
        if (lower.endsWith("/forgot-password")) return "Parol bərpa sorğusu";
        if (lower.endsWith("/reset-password")) return "Parol sıfırlama";
        if (lower.endsWith("/password")) return "Parol dəyişmə";
        if (lower.endsWith("/activate")) return "Aktivləşdirmə";
        if (lower.endsWith("/submit")) return "İmtahan təqdimi";
        if (lower.contains("/notifications/read")) return "Oxundu işarələmə";
        if (lower.contains("/start") || lower.endsWith("/sessions")) return "İmtahan başlatma";
        switch (method) {
            case "GET": return "Baxış";
            case "POST": return "Yaratma";
            case "PUT":
            case "PATCH": return "Yeniləmə";
            case "DELETE": return "Silmə";
            default: return method;
        }
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        String real = request.getHeader("X-Real-IP");
        if (real != null && !real.isBlank()) return real.trim();
        return request.getRemoteAddr();
    }
}
