package com.novis.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        boolean mfaRequired,
        String mfaSessionToken
) {}
