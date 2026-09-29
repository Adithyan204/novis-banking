package com.novis.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novis.account.entity.Account;
import com.novis.account.repository.AccountRepository;
import com.novis.admin.dto.AccountReconciliation;
import com.novis.admin.dto.ReconciliationReport;
import com.novis.audit.service.AuditService;
import com.novis.transaction.service.LedgerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReconciliationJob {
    private static final Logger log = LoggerFactory.getLogger(ReconciliationJob.class);


    private final AccountRepository accountRepository;
    private final LedgerService ledgerService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(cron = "${novis.reconciliation.cron:0 0 2 * * *}")
    public ReconciliationReport runReconciliation() {
        log.info("Starting daily reconciliation job");
        List<Account> activeAccounts = accountRepository.findAll().stream()
                .filter(a -> a.getStatus() == Account.AccountStatus.ACTIVE)
                .toList();

        List<AccountReconciliation> inconsistencies = new ArrayList<>();

        for (Account account : activeAccounts) {
            BigDecimal computedBalance = ledgerService.getBalance(account.getId());
            // In a real system, you might compare against an external core banking system
            // Here we just check internal consistency or simulate a check.
            boolean isConsistent = true; // Placeholder for actual logic

            Map<String, Object> details = new HashMap<>();
            details.put("accountId", account.getId());
            details.put("computedBalance", computedBalance);
            details.put("isConsistent", isConsistent);

            try {
                String detailsJson = objectMapper.writeValueAsString(details);
                auditService.log(null, "RECONCILIATION_RUN", "Account", account.getId(), detailsJson, null);

                if (!isConsistent) {
                    auditService.log(null, "RECONCILIATION_MISMATCH", "Account", account.getId(), "Mismatch detected! " + detailsJson, null);
                    inconsistencies.add(new AccountReconciliation(account.getId(), account.getAccountNumber(), computedBalance, false));
                }
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize reconciliation details", e);
            }
        }

        log.info("Reconciliation job completed. Inconsistencies found: {}", inconsistencies.size());
        return new ReconciliationReport(LocalDateTime.now(), inconsistencies);
    }

    public ReconciliationJob(AccountRepository accountRepository, LedgerService ledgerService, AuditService auditService) {
        this.accountRepository = accountRepository;
        this.ledgerService = ledgerService;
        this.auditService = auditService;
    }
}
