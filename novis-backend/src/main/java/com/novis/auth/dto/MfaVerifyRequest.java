package com.novis.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MfaVerifyRequest(
        @NotBlank @Email String email,
        @NotBlank String otp
) {}
