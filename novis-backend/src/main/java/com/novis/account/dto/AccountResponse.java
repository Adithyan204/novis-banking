package com.novis.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        Long id,
        String accountNumber,
        String accountType,
        String currency,
        String status,
        BigDecimal balance,
        LocalDateTime createdAt
) {}
