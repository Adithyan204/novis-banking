package com.novis.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReconciliationReport(
        LocalDateTime generatedAt,
        List<AccountReconciliation> inconsistencies
) {}
