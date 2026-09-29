package com.novis.transaction.entity;

import com.novis.account.entity.Account;
import com.novis.auth.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private Account toAccount;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
        private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
        private TransactionStatus status = TransactionStatus.PENDING;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by", nullable = false)
    private User initiatedBy;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED, FLAGGED
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Account fromAccount) {
        this.fromAccount = fromAccount;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public void setToAccount(Account toAccount) {
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(User initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Transaction() {
        this.currency = "USD";
        this.status = TransactionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public Transaction(Long id, String idempotencyKey, Account fromAccount, Account toAccount, BigDecimal amount, String currency, TransactionStatus status, String description, User initiatedBy, String ipAddress, LocalDateTime createdAt, LocalDateTime completedAt) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.description = description;
        this.initiatedBy = initiatedBy;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String idempotencyKey;
        private Account fromAccount;
        private Account toAccount;
        private BigDecimal amount;
        private String currency = "USD";
        private TransactionStatus status = TransactionStatus.PENDING;
        private String description;
        private User initiatedBy;
        private String ipAddress;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime completedAt;
        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public Builder fromAccount(Account fromAccount) {
            this.fromAccount = fromAccount;
            return this;
        }

        public Builder toAccount(Account toAccount) {
            this.toAccount = toAccount;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder status(TransactionStatus status) {
            this.status = status;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder initiatedBy(User initiatedBy) {
            this.initiatedBy = initiatedBy;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder completedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Transaction build() {
            Transaction obj = new Transaction();
            obj.id = this.id;
            obj.idempotencyKey = this.idempotencyKey;
            obj.fromAccount = this.fromAccount;
            obj.toAccount = this.toAccount;
            obj.amount = this.amount;
            obj.currency = this.currency;
            obj.status = this.status;
            obj.description = this.description;
            obj.initiatedBy = this.initiatedBy;
            obj.ipAddress = this.ipAddress;
            obj.createdAt = this.createdAt;
            obj.completedAt = this.completedAt;
            return obj;
        }
    }
}
