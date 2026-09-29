package com.novis.audit.service;

import com.novis.audit.entity.AuditLog;
import com.novis.audit.repository.AuditLogRepository;
import com.novis.auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void log(Long userId, String action, String entityType, Long entityId, String details, HttpServletRequest request) {
        AuditLog.Builder builder = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details);

        if (userId != null) {
            userRepository.findById(userId).ifPresent(builder::user);
        }

        if (request != null) {
            builder.ipAddress(request.getRemoteAddr());
            builder.userAgent(request.getHeader("User-Agent"));
        }

        auditLogRepository.save(builder.build());
    }

    public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }
}
