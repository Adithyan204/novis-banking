package com.novis.fraud.service;

import com.novis.fraud.entity.FraudFlag;
import com.novis.transaction.entity.Transaction;
import com.novis.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class LargeAmountRule implements FraudRule {

    private final TransactionRepository transactionRepository;

    @Override
    public Result evaluate(Transaction transaction) {
        if (transaction.getFromAccount() == null) {
            return new Result(false, "LargeAmountRule", null, null);
        }

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Transaction> recentTransactions = transactionRepository.findByFromAccountIdAndCreatedAtAfter(
                transaction.getFromAccount().getId(), thirtyDaysAgo);

        if (recentTransactions.isEmpty()) {
            if (transaction.getAmount().compareTo(new BigDecimal("10000")) > 0) {
                return new Result(true, "LargeAmountRule", FraudFlag.Severity.MEDIUM, "First transfer exceeds 10000");
            }
            return new Result(false, "LargeAmountRule", null, null);
        }

        BigDecimal totalAmount = recentTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageAmount = totalAmount.divide(new BigDecimal(recentTransactions.size()), 2, RoundingMode.HALF_UP);

        if (transaction.getAmount().compareTo(averageAmount.multiply(new BigDecimal("3"))) > 0) {
            return new Result(true, "LargeAmountRule", FraudFlag.Severity.HIGH, "Amount is more than 3x the 30-day average");
        }

        return new Result(false, "LargeAmountRule", null, null);
    }

    public LargeAmountRule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
}
