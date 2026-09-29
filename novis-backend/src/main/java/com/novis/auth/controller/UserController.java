package com.novis.auth.controller;

import com.novis.audit.entity.AuditLog;
import com.novis.audit.repository.AuditLogRepository;
import com.novis.auth.dto.*;
import com.novis.auth.entity.User;
import com.novis.auth.repository.UserRepository;
import com.novis.common.exception.ResourceNotFoundException;
import com.novis.common.exception.UnauthorizedException;
import com.novis.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogRepository auditLogRepository;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogRepository = auditLogRepository;
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getProfile() {
        User user = currentUser();
        return ApiResponse.success(toResponse(user));
    }

    @PutMapping("/me")
    public ApiResponse<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest req) {
        User user = currentUser();
        user.setFullName(req.fullName());
        userRepository.save(user);
        return ApiResponse.success("Profile updated", toResponse(user));
    }

    @PostMapping("/me/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        User user = currentUser();
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
        return ApiResponse.success("Password changed successfully", null);
    }

    @GetMapping("/me/activity")
    public ApiResponse<List<ActivityEntry>> getActivity() {
        User user = currentUser();
        List<AuditLog> logs = auditLogRepository.findByUserOrderByCreatedAtDesc(
                user, PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<ActivityEntry> entries = logs.stream().map(log -> new ActivityEntry(
                log.getId(),
                log.getAction(),
                log.getEntityType(),
                log.getDetails(),
                log.getIpAddress(),
                log.getCreatedAt()
        )).toList();
        return ApiResponse.success(entries);
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.getKycStatus().name(),
                user.isMfaEnabled(),
                user.getCreatedAt()
        );
    }
}
