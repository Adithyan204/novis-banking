package com.novis.admin.dto;

import java.math.BigDecimal;

public record AccountReconciliation(
        Long accountId,
        String accountNumber,
        BigDecimal computedBalance,
        boolean isConsistent
) {}
