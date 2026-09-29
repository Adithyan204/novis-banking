package com.novis.fraud.service;

import com.novis.account.entity.Account;
import com.novis.fraud.entity.FraudFlag;
import com.novis.transaction.entity.Transaction;
import com.novis.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class FraudRuleEngineTest {

    @Mock
    private TransactionRepository transactionRepository;

    private LargeAmountRule largeAmountRule;
    private RapidTransferRule rapidTransferRule;
    private NewBeneficiaryRule newBeneficiaryRule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        largeAmountRule = new LargeAmountRule(transactionRepository);
        rapidTransferRule = new RapidTransferRule(transactionRepository);
        newBeneficiaryRule = new NewBeneficiaryRule(transactionRepository);
    }

    @Test
    void testLargeAmountRule_flagsLargeTransaction() {
        Account from = Account.builder().id(1L).build();
        Transaction t = Transaction.builder().fromAccount(from).amount(new BigDecimal("400")).build();
        
        when(transactionRepository.findByFromAccountIdAndCreatedAtAfter(eq(1L), any()))
                .thenReturn(List.of(
                        Transaction.builder().amount(new BigDecimal("100")).build(),
                        Transaction.builder().amount(new BigDecimal("100")).build()
                )); // Average 100

        FraudRule.Result result = largeAmountRule.evaluate(t);
        assertTrue(result.flagged());
        assertEquals(FraudFlag.Severity.HIGH, result.severity());
    }

    @Test
    void testLargeAmountRule_normalTransaction_notFlagged() {
        Account from = Account.builder().id(1L).build();
        Transaction t = Transaction.builder().fromAccount(from).amount(new BigDecimal("200")).build();

        when(transactionRepository.findByFromAccountIdAndCreatedAtAfter(eq(1L), any()))
                .thenReturn(List.of(
                        Transaction.builder().amount(new BigDecimal("100")).build()
                ));

        FraudRule.Result result = largeAmountRule.evaluate(t);
        assertFalse(result.flagged());
    }

    @Test
    void testRapidTransferRule_flagsRapidTransfers() {
        Account from = Account.builder().id(1L).build();
        Transaction t = Transaction.builder().fromAccount(from).build();

        when(transactionRepository.countByFromAccountIdAndCreatedAtAfter(eq(1L), any()))
                .thenReturn(5L);

        FraudRule.Result result = rapidTransferRule.evaluate(t);
        assertTrue(result.flagged());
    }

    @Test
    void testNewBeneficiaryRule_flagsNewBeneficiary() {
        Account from = Account.builder().id(1L).build();
        Account to = Account.builder().id(2L).build();
        Transaction t = Transaction.builder().fromAccount(from).toAccount(to).amount(new BigDecimal("6000")).build();

        when(transactionRepository.countByFromAccountIdAndToAccountIdAndStatus(eq(1L), eq(2L), eq(Transaction.TransactionStatus.COMPLETED)))
                .thenReturn(0L);

        FraudRule.Result result = newBeneficiaryRule.evaluate(t);
        assertTrue(result.flagged());
        assertEquals(FraudFlag.Severity.MEDIUM, result.severity());
    }

    @Test
    void testNewBeneficiaryRule_knownBeneficiary_notFlagged() {
        Account from = Account.builder().id(1L).build();
        Account to = Account.builder().id(2L).build();
        Transaction t = Transaction.builder().fromAccount(from).toAccount(to).amount(new BigDecimal("6000")).build();

        when(transactionRepository.countByFromAccountIdAndToAccountIdAndStatus(eq(1L), eq(2L), eq(Transaction.TransactionStatus.COMPLETED)))
                .thenReturn(1L);

        FraudRule.Result result = newBeneficiaryRule.evaluate(t);
        assertFalse(result.flagged());
    }
}
