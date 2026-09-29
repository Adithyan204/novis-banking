package com.novis.auth.dto;
import jakarta.validation.constraints.NotBlank;
public record UpdateProfileRequest(@NotBlank String fullName) {}
