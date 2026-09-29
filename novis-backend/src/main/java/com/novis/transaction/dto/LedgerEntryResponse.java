package com.novis.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LedgerEntryResponse(
        Long id,
        String entryType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        LocalDateTime createdAt
) {}
