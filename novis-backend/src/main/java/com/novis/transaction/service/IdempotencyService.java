package com.novis.transaction.service;

import com.novis.transaction.dto.TransactionResponse;
import com.novis.transaction.entity.Transaction;
import com.novis.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Supplier;

@Service
public class IdempotencyService {

    private final TransactionRepository transactionRepository;

    @Transactional
    public TransactionResponse checkAndStore(String key, Long userId, Supplier<TransactionResponse> action) {
        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(key);
        if (existing.isPresent()) {
            Transaction t = existing.get();
            return new TransactionResponse(t.getId(), t.getIdempotencyKey(),
                    t.getFromAccount() != null ? t.getFromAccount().getId() : null,
                    t.getToAccount() != null ? t.getToAccount().getId() : null,
                    t.getAmount(), t.getCurrency(), t.getStatus().name(),
                    t.getDescription(), t.getCreatedAt());
        }
        return action.get();
    }

    public IdempotencyService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
}
