package com.novis.fraud.service;

import com.novis.fraud.entity.FraudFlag;
import com.novis.transaction.entity.Transaction;
import com.novis.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class NewBeneficiaryRule implements FraudRule {

    private final TransactionRepository transactionRepository;

    @Override
    public Result evaluate(Transaction transaction) {
        if (transaction.getFromAccount() == null || transaction.getToAccount() == null) {
            return new Result(false, "NewBeneficiaryRule", null, null);
        }

        long previousTransfers = transactionRepository.countByFromAccountIdAndToAccountIdAndStatus(
                transaction.getFromAccount().getId(),
                transaction.getToAccount().getId(),
                Transaction.TransactionStatus.COMPLETED
        );

        if (previousTransfers == 0 && transaction.getAmount().compareTo(new BigDecimal("5000")) > 0) {
            return new Result(true, "NewBeneficiaryRule", FraudFlag.Severity.MEDIUM, "First time transfer to this account exceeding 5000");
        }

        return new Result(false, "NewBeneficiaryRule", null, null);
    }

    public NewBeneficiaryRule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
}
