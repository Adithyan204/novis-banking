package com.novis.kyc.dto;

import java.time.LocalDateTime;

public record KycDocumentResponse(
        Long id,
        String documentType,
        String status,
        String rejectionReason,
        LocalDateTime createdAt
) {}
