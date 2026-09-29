package com.novis.transaction.service;

import com.novis.account.entity.Account;
import com.novis.transaction.entity.LedgerEntry;
import com.novis.transaction.entity.Transaction;
import com.novis.transaction.repository.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional
    public void createLedgerEntries(Transaction transaction, BigDecimal fromBalance, BigDecimal toBalance) {
        if (transaction.getFromAccount() != null) {
            BigDecimal newFromBalance = fromBalance.subtract(transaction.getAmount());
            LedgerEntry debitEntry = LedgerEntry.builder()
                    .transaction(transaction)
                    .account(transaction.getFromAccount())
                    .entryType(LedgerEntry.EntryType.DEBIT)
                    .amount(transaction.getAmount())
                    .balanceAfter(newFromBalance)
                    .build();
            ledgerEntryRepository.save(debitEntry);
        }

        if (transaction.getToAccount() != null) {
            BigDecimal newToBalance = toBalance.add(transaction.getAmount());
            LedgerEntry creditEntry = LedgerEntry.builder()
                    .transaction(transaction)
                    .account(transaction.getToAccount())
                    .entryType(LedgerEntry.EntryType.CREDIT)
                    .amount(transaction.getAmount())
                    .balanceAfter(newToBalance)
                    .build();
            ledgerEntryRepository.save(creditEntry);
        }
    }

    public BigDecimal getBalance(Long accountId) {
        BigDecimal totalCredit = ledgerEntryRepository.sumAmountByAccountIdAndEntryType(accountId, LedgerEntry.EntryType.CREDIT);
        BigDecimal totalDebit = ledgerEntryRepository.sumAmountByAccountIdAndEntryType(accountId, LedgerEntry.EntryType.DEBIT);
        return totalCredit.subtract(totalDebit);
    }

    public List<LedgerEntry> getEntriesForAccount(Long accountId) {
        return ledgerEntryRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    public LedgerService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }
}
