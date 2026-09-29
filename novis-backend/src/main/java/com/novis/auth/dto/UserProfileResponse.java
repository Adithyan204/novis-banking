package com.novis.auth.dto;
import java.time.LocalDateTime;
public record UserProfileResponse(
    Long id,
    String email,
    String fullName,
    String role,
    String kycStatus,
    boolean mfaEnabled,
    LocalDateTime createdAt
) {}
