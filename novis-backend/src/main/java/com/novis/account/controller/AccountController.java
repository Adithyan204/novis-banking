package com.novis.account.controller;

import com.novis.account.dto.AccountResponse;
import com.novis.account.dto.AccountSummaryResponse;
import com.novis.account.entity.Account;
import com.novis.account.service.AccountService;
import com.novis.auth.entity.User;
import com.novis.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountSummaryResponse>> getMyAccounts(@AuthenticationPrincipal User user) {
        List<Account> accounts = accountService.getMyAccounts(user.getId());
        List<AccountResponse> responses = accounts.stream()
                .map(a -> new AccountResponse(a.getId(), a.getAccountNumber(), a.getAccountType().name(),
                        a.getCurrency(), a.getStatus().name(), a.getBalance(), a.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(new AccountSummaryResponse(responses)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Account a = accountService.getAccountById(id, user.getId());
        AccountResponse response = new AccountResponse(a.getId(), a.getAccountNumber(), a.getAccountType().name(),
                a.getCurrency(), a.getStatus().name(), a.getBalance(), a.getCreatedAt());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
}
