package com.novis.kyc.controller;

import com.novis.auth.entity.User;
import com.novis.common.response.ApiResponse;
import com.novis.kyc.dto.KycStatusResponse;
import com.novis.kyc.service.KycService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/kyc")
public class KycController {

    private final KycService kycService;

    @PostMapping("/documents")
    public ResponseEntity<ApiResponse<Void>> uploadDocument(
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) throws IOException {
        kycService.uploadDocument(user.getId(), documentType, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Document uploaded successfully", null));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<KycStatusResponse>> getStatus(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(kycService.getStatus(user.getId())));
    }

    public KycController(KycService kycService) {
        this.kycService = kycService;
    }
}
