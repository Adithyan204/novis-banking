package com.novis.auth.dto;
import java.time.LocalDateTime;
public record ActivityEntry(
    Long id,
    String action,
    String entityType,
    String details,
    String ipAddress,
    LocalDateTime createdAt
) {}
