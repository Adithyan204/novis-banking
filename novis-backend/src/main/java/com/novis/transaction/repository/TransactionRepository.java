package com.novis.transaction.repository;

import com.novis.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByFromAccountIdOrToAccountId(Long fromId, Long toId, Pageable pageable);
    List<Transaction> findByFromAccountIdAndCreatedAtAfter(Long accountId, LocalDateTime after);
    long countByFromAccountIdAndCreatedAtAfter(Long accountId, LocalDateTime after);
    long countByFromAccountIdAndToAccountIdAndStatus(Long fromId, Long toId, Transaction.TransactionStatus status);
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
