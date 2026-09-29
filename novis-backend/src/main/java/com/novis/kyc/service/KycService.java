package com.novis.kyc.service;

import com.novis.audit.service.AuditService;
import com.novis.auth.entity.User;
import com.novis.auth.repository.UserRepository;
import com.novis.common.exception.ResourceNotFoundException;
import com.novis.kyc.dto.KycDocumentResponse;
import com.novis.kyc.dto.KycStatusResponse;
import com.novis.kyc.entity.KycDocument;
import com.novis.kyc.repository.KycDocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KycService {

    private final KycDocumentRepository kycDocumentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Value("${kyc.upload-dir}")
    private String uploadDir;

    @Transactional
    public void uploadDocument(Long userId, String documentType, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId).orElseThrow();

        Path userDir = Paths.get(uploadDir, userId.toString());
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }

        String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String filename = UUID.randomUUID() + extension;
        Path filePath = userDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        KycDocument document = KycDocument.builder()
                .user(user)
                .documentType(KycDocument.DocumentType.valueOf(documentType.toUpperCase()))
                .filePath(filePath.toString())
                .originalFilename(file.getOriginalFilename())
                .build();
        
        kycDocumentRepository.save(document);
        
        user.setKycStatus(User.KycStatus.PENDING);
        userRepository.save(user);

        auditService.log(userId, "KYC_UPLOADED", "KycDocument", document.getId(), "Uploaded " + documentType, null);
    }

    public KycStatusResponse getStatus(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<KycDocument> documents = kycDocumentRepository.findByUserId(userId);

        List<KycDocumentResponse> docResponses = documents.stream()
                .map(d -> new KycDocumentResponse(d.getId(), d.getDocumentType().name(), d.getStatus().name(), d.getRejectionReason(), d.getCreatedAt()))
                .collect(Collectors.toList());

        return new KycStatusResponse(userId, user.getKycStatus().name(), docResponses);
    }

    @Transactional
    public void reviewDocument(Long documentId, String action, String rejectionReason, Long adminId) {
        KycDocument document = kycDocumentRepository.findById(documentId).orElseThrow();
        User admin = userRepository.findById(adminId).orElseThrow();
        User user = document.getUser();

        document.setReviewedBy(admin);
        document.setReviewedAt(LocalDateTime.now());

        if ("APPROVED".equalsIgnoreCase(action)) {
            document.setStatus(KycDocument.DocumentStatus.APPROVED);
            auditService.log(adminId, "KYC_APPROVED", "KycDocument", document.getId(), "Approved by admin", null);
        } else {
            document.setStatus(KycDocument.DocumentStatus.REJECTED);
            document.setRejectionReason(rejectionReason);
            user.setKycStatus(User.KycStatus.REJECTED);
            userRepository.save(user);
            auditService.log(adminId, "KYC_REJECTED", "KycDocument", document.getId(), "Rejected: " + rejectionReason, null);
        }

        kycDocumentRepository.save(document);

        // Check if all docs are approved to set user status
        if ("APPROVED".equalsIgnoreCase(action)) {
            List<KycDocument> allUserDocs = kycDocumentRepository.findByUserId(user.getId());
            boolean allApproved = allUserDocs.stream().allMatch(d -> d.getStatus() == KycDocument.DocumentStatus.APPROVED);
            if (allApproved && !allUserDocs.isEmpty()) {
                user.setKycStatus(User.KycStatus.VERIFIED);
                userRepository.save(user);
            }
        }
    }

    public KycService(KycDocumentRepository kycDocumentRepository, UserRepository userRepository, AuditService auditService) {
        this.kycDocumentRepository = kycDocumentRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }
}
