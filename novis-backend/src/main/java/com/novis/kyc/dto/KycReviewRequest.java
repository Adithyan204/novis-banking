package com.novis.kyc.dto;

import jakarta.validation.constraints.NotBlank;

public record KycReviewRequest(
        @NotBlank String action, // APPROVED or REJECTED
        String rejectionReason
) {}
