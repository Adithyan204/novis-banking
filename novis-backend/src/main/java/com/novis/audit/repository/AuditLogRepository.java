package com.novis.audit.repository;

import com.novis.audit.entity.AuditLog;
import com.novis.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<AuditLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
