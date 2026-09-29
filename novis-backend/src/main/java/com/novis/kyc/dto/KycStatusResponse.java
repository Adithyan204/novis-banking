package com.novis.kyc.dto;

import java.util.List;

public record KycStatusResponse(
        Long userId,
        String kycStatus,
        List<KycDocumentResponse> documents
) {}
