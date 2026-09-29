package com.novis.admin.controller;

import com.novis.admin.dto.ReconciliationReport;
import com.novis.audit.entity.AuditLog;
import com.novis.audit.repository.AuditLogRepository;
import com.novis.auth.entity.User;
import com.novis.common.response.ApiResponse;
import com.novis.fraud.dto.FraudFlagResponse;
import com.novis.fraud.dto.ResolveFlagRequest;
import com.novis.fraud.entity.FraudFlag;
import com.novis.fraud.repository.FraudFlagRepository;
import com.novis.kyc.dto.KycDocumentResponse;
import com.novis.kyc.dto.KycReviewRequest;
import com.novis.kyc.entity.KycDocument;
import com.novis.kyc.repository.KycDocumentRepository;
import com.novis.kyc.service.KycService;
import com.novis.scheduler.ReconciliationJob;
import com.novis.transaction.entity.Transaction;
import com.novis.transaction.repository.TransactionRepository;
import com.novis.transaction.service.LedgerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final KycDocumentRepository kycDocumentRepository;
    private final KycService kycService;
    private final FraudFlagRepository fraudFlagRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerService ledgerService;
    private final AuditLogRepository auditLogRepository;
    private final ReconciliationJob reconciliationJob;

    @GetMapping("/kyc/pending")
    public ResponseEntity<ApiResponse<List<KycDocumentResponse>>> getPendingKyc() {
        List<KycDocument> pending = kycDocumentRepository.findByStatus(KycDocument.DocumentStatus.PENDING);
        List<KycDocumentResponse> responses = pending.stream()
                .map(d -> new KycDocumentResponse(d.getId(), d.getDocumentType().name(), d.getStatus().name(), d.getRejectionReason(), d.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/kyc/{id}/review")
    public ResponseEntity<ApiResponse<Void>> reviewKyc(@PathVariable Long id, @RequestBody KycReviewRequest request, @AuthenticationPrincipal User admin) {
        kycService.reviewDocument(id, request.action(), request.rejectionReason(), admin.getId());
        return ResponseEntity.ok(ApiResponse.success("Document reviewed", null));
    }

    @GetMapping("/fraud/flags")
    public ResponseEntity<ApiResponse<List<FraudFlagResponse>>> getPendingFraudFlags() {
        List<FraudFlag> pending = fraudFlagRepository.findByStatus(FraudFlag.FlagStatus.PENDING_REVIEW);
        List<FraudFlagResponse> responses = pending.stream()
                .map(f -> new FraudFlagResponse(f.getId(), f.getTransaction().getId(), f.getRuleTriggered(), f.getSeverity().name(), f.getStatus().name(), f.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/fraud/flags/{id}/resolve")
    public ResponseEntity<ApiResponse<Void>> resolveFraudFlag(@PathVariable Long id, @RequestBody ResolveFlagRequest request, @AuthenticationPrincipal User admin) {
        FraudFlag flag = fraudFlagRepository.findById(id).orElseThrow();
        Transaction transaction = flag.getTransaction();

        flag.setStatus(FraudFlag.FlagStatus.valueOf(request.action().toUpperCase()));
        flag.setReviewNote(request.reviewNote());
        flag.setReviewedBy(admin);
        flag.setReviewedAt(LocalDateTime.now());
        fraudFlagRepository.save(flag);

        if ("APPROVED".equalsIgnoreCase(request.action())) {
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            // Need fromBalance and toBalance, but for simplicity in resolve:
            ledgerService.createLedgerEntries(transaction, ledgerService.getBalance(transaction.getFromAccount().getId()), ledgerService.getBalance(transaction.getToAccount().getId()));
        } else {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
        }
        transactionRepository.save(transaction);

        return ResponseEntity.ok(ApiResponse.success("Fraud flag resolved", null));
    }

    @GetMapping("/reconciliation")
    public ResponseEntity<ApiResponse<ReconciliationReport>> runReconciliation() {
        ReconciliationReport report = reconciliationJob.runReconciliation();
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogs(@RequestParam(required = false) Long userId, Pageable pageable) {
        Page<AuditLog> logs;
        if (userId != null) {
            logs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        } else {
            logs = auditLogRepository.findAll(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    public AdminController(KycDocumentRepository kycDocumentRepository, KycService kycService, FraudFlagRepository fraudFlagRepository, TransactionRepository transactionRepository, LedgerService ledgerService, AuditLogRepository auditLogRepository, ReconciliationJob reconciliationJob) {
        this.kycDocumentRepository = kycDocumentRepository;
        this.kycService = kycService;
        this.fraudFlagRepository = fraudFlagRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerService = ledgerService;
        this.auditLogRepository = auditLogRepository;
        this.reconciliationJob = reconciliationJob;
    }
}
