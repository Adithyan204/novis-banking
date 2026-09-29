package com.novis.fraud.service;

import com.novis.fraud.entity.FraudFlag;
import com.novis.transaction.entity.Transaction;
import com.novis.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RapidTransferRule implements FraudRule {

    private final TransactionRepository transactionRepository;

    @Override
    public Result evaluate(Transaction transaction) {
        if (transaction.getFromAccount() == null) {
            return new Result(false, "RapidTransferRule", null, null);
        }

        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        long count = transactionRepository.countByFromAccountIdAndCreatedAtAfter(
                transaction.getFromAccount().getId(), tenMinutesAgo);

        if (count >= 5) {
            return new Result(true, "RapidTransferRule", FraudFlag.Severity.HIGH, "More than 5 transfers in the last 10 minutes");
        }

        return new Result(false, "RapidTransferRule", null, null);
    }

    public RapidTransferRule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
}
