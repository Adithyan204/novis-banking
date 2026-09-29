package com.novis.fraud.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveFlagRequest(
        @NotBlank String action, // APPROVE or REJECT
        String reviewNote
) {}
