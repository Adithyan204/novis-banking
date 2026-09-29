package com.novis.account.dto;

import java.util.List;

public record AccountSummaryResponse(
        List<AccountResponse> accounts
) {}
