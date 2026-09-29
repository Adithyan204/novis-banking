package com.novis.transaction.service;

import com.novis.account.entity.Account;
import com.novis.account.repository.AccountRepository;
import com.novis.audit.service.AuditService;
import com.novis.auth.entity.User;
import com.novis.auth.repository.UserRepository;
import com.novis.common.exception.InsufficientFundsException;
import com.novis.common.exception.KycNotVerifiedException;
import com.novis.common.exception.ResourceNotFoundException;
import com.novis.common.exception.UnauthorizedException;
import com.novis.fraud.service.FraudRule;
import com.novis.fraud.service.FraudRuleEngine;
import com.novis.transaction.dto.TransactionResponse;
import com.novis.transaction.dto.TransferRequest;
import com.novis.transaction.entity.Transaction;
import com.novis.transaction.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerService ledgerService;
    private final IdempotencyService idempotencyService;
    private final UserRepository userRepository;
    private final FraudRuleEngine fraudRuleEngine;
    private final AuditService auditService;

    @Transactional
    public TransactionResponse transfer(TransferRequest req, String idempotencyKey, Long userId, HttpServletRequest httpReq) {
        return idempotencyService.checkAndStore(idempotencyKey, userId, () -> {
            User user = userRepository.findById(userId).orElseThrow();
            if (user.getKycStatus() != User.KycStatus.VERIFIED) {
                throw new KycNotVerifiedException("KYC is not verified");
            }

            Account fromAccount = accountRepository.findByIdForUpdate(req.fromAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("From account not found"));
            Account toAccount = accountRepository.findById(req.toAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("To account not found"));

            if (!fromAccount.getUser().getId().equals(userId)) {
                throw new UnauthorizedException("You do not own the from account");
            }
            if (fromAccount.getStatus() != Account.AccountStatus.ACTIVE) {
                throw new UnauthorizedException("From account is not active");
            }

            BigDecimal fromBalance = ledgerService.getBalance(fromAccount.getId());
            if (fromBalance.compareTo(req.amount()) < 0) {
                throw new InsufficientFundsException("Insufficient funds");
            }
            BigDecimal toBalance = ledgerService.getBalance(toAccount.getId());

            Transaction transaction = Transaction.builder()
                    .idempotencyKey(idempotencyKey)
                    .fromAccount(fromAccount)
                    .toAccount(toAccount)
                    .amount(req.amount())
                    .currency(req.currency())
                    .description(req.description())
                    .initiatedBy(user)
                    .ipAddress(httpReq.getRemoteAddr())
                    .build();
            transaction = transactionRepository.save(transaction);

            List<FraudRule.Result> fraudResults = fraudRuleEngine.evaluate(transaction);
            if (!fraudResults.isEmpty()) {
                transaction.setStatus(Transaction.TransactionStatus.FLAGGED);
                transactionRepository.save(transaction);
                fraudRuleEngine.createFlags(transaction, fraudResults);
                auditService.log(userId, "TRANSFER_FLAGGED", "Transaction", transaction.getId(), "Fraud detected", httpReq);
            } else {
                ledgerService.createLedgerEntries(transaction, fromBalance, toBalance);
                transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
                transaction.setCompletedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
                auditService.log(userId, "TRANSFER_COMPLETED", "Transaction", transaction.getId(), "Transfer completed", httpReq);
            }

            return new TransactionResponse(
                    transaction.getId(),
                    transaction.getIdempotencyKey(),
                    transaction.getFromAccount().getId(),
                    transaction.getToAccount().getId(),
                    transaction.getAmount(),
                    transaction.getCurrency(),
                    transaction.getStatus().name(),
                    transaction.getDescription(),
                    transaction.getCreatedAt()
            );
        });
    }

    public TransferService(AccountRepository accountRepository, TransactionRepository transactionRepository, LedgerService ledgerService, IdempotencyService idempotencyService, UserRepository userRepository, FraudRuleEngine fraudRuleEngine, AuditService auditService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerService = ledgerService;
        this.idempotencyService = idempotencyService;
        this.userRepository = userRepository;
        this.fraudRuleEngine = fraudRuleEngine;
        this.auditService = auditService;
    }
}
