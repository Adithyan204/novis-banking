package com.novis.auth.controller;

import com.novis.auth.dto.LoginRequest;
import com.novis.auth.dto.MfaVerifyRequest;
import com.novis.auth.dto.RefreshRequest;
import com.novis.auth.dto.RegisterRequest;
import com.novis.auth.dto.TokenResponse;
import com.novis.auth.service.AuthService;
import com.novis.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpReq) {
        authService.register(request, httpReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User registered successfully", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpReq) {
        TokenResponse response = authService.login(request, httpReq);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<ApiResponse<TokenResponse>> verifyMfa(@Valid @RequestBody MfaVerifyRequest request, HttpServletRequest httpReq) {
        TokenResponse response = authService.verifyMfa(request, httpReq);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenResponse response = authService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token, HttpServletRequest request) {
        if (token != null && token.startsWith("Bearer ")) {
            authService.logout(token.substring(7), request);
        }
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
}
