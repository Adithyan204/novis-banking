package com.novis.transaction.controller;

import com.novis.auth.entity.User;
import com.novis.common.response.ApiResponse;
import com.novis.transaction.dto.TransactionResponse;
import com.novis.transaction.dto.TransferRequest;
import com.novis.transaction.service.TransferService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) {
        TransactionResponse response = transferService.transfer(request, idempotencyKey, user.getId(), httpReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }
}
