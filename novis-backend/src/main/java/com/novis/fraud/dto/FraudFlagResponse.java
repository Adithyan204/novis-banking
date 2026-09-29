package com.novis.fraud.dto;

import java.time.LocalDateTime;

public record FraudFlagResponse(
        Long id,
        Long transactionId,
        String ruleTriggered,
        String severity,
        String status,
        LocalDateTime createdAt
) {}
