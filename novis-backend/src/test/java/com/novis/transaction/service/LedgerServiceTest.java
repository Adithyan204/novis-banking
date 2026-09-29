package com.novis.transaction.service;

import com.novis.account.entity.Account;
import com.novis.transaction.entity.LedgerEntry;
import com.novis.transaction.entity.Transaction;
import com.novis.transaction.repository.LedgerEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LedgerServiceTest {

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks
    private LedgerService ledgerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testComputeBalance_withDebitsAndCredits() {
        Long accountId = 1L;
        when(ledgerEntryRepository.sumAmountByAccountIdAndEntryType(accountId, LedgerEntry.EntryType.CREDIT))
                .thenReturn(new BigDecimal("1000.00"));
        when(ledgerEntryRepository.sumAmountByAccountIdAndEntryType(accountId, LedgerEntry.EntryType.DEBIT))
                .thenReturn(new BigDecimal("300.00"));

        BigDecimal balance = ledgerService.getBalance(accountId);
        assertEquals(new BigDecimal("700.00"), balance);
    }

    @Test
    void testComputeBalance_emptyAccount_returnsZero() {
        Long accountId = 2L;
        when(ledgerEntryRepository.sumAmountByAccountIdAndEntryType(accountId, LedgerEntry.EntryType.CREDIT))
                .thenReturn(BigDecimal.ZERO);
        when(ledgerEntryRepository.sumAmountByAccountIdAndEntryType(accountId, LedgerEntry.EntryType.DEBIT))
                .thenReturn(BigDecimal.ZERO);

        BigDecimal balance = ledgerService.getBalance(accountId);
        assertEquals(BigDecimal.ZERO, balance);
    }
}
