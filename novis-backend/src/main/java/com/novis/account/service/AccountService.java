package com.novis.account.service;

import com.novis.account.entity.Account;
import com.novis.account.repository.AccountRepository;
import com.novis.auth.entity.User;
import com.novis.auth.repository.UserRepository;
import com.novis.common.exception.ResourceNotFoundException;
import com.novis.common.exception.UnauthorizedException;
import com.novis.transaction.service.LedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final LedgerService ledgerService;
    private final UserRepository userRepository;

    public List<Account> getMyAccounts(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        accounts.forEach(account -> account.setBalance(ledgerService.getBalance(account.getId())));
        return accounts;
    }

    public Account getAccountById(Long accountId, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Not authorized to view this account");
        }
        account.setBalance(ledgerService.getBalance(account.getId()));
        return account;
    }

    public BigDecimal computeBalance(Long accountId) {
        return ledgerService.getBalance(accountId);
    }

    @Transactional
    public Account createAccount(Long userId, String type) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account account = Account.builder()
                .user(user)
                .accountNumber(generateAccountNumber())
                .accountType(Account.AccountType.valueOf(type.toUpperCase()))
                .build();
        return accountRepository.save(account);
    }

    private String generateAccountNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public AccountService(AccountRepository accountRepository, LedgerService ledgerService, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.ledgerService = ledgerService;
        this.userRepository = userRepository;
    }
}
