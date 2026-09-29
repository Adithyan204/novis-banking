package com.novis.account.entity;

import com.novis.auth.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
        private AccountType accountType = AccountType.CHECKING;

    @Column(nullable = false)
        private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
        private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt = LocalDateTime.now();

    @Transient
    private BigDecimal balance; // Computed by LedgerService

    public enum AccountType {
        CHECKING, SAVINGS
    }

    public enum AccountStatus {
        ACTIVE, FROZEN, CLOSED
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Account() {
        this.accountType = AccountType.CHECKING;
        this.currency = "USD";
        this.status = AccountStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    public Account(Long id, User user, String accountNumber, AccountType accountType, String currency, AccountStatus status, LocalDateTime createdAt, BigDecimal balance) {
        this.id = id;
        this.user = user;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
        this.balance = balance;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private User user;
        private String accountNumber;
        private AccountType accountType = AccountType.CHECKING;
        private String currency = "USD";
        private AccountStatus status = AccountStatus.ACTIVE;
        private LocalDateTime createdAt = LocalDateTime.now();
        private BigDecimal balance;
        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder accountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public Builder accountType(AccountType accountType) {
            this.accountType = accountType;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder status(AccountStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder balance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public Account build() {
            Account obj = new Account();
            obj.id = this.id;
            obj.user = this.user;
            obj.accountNumber = this.accountNumber;
            obj.accountType = this.accountType;
            obj.currency = this.currency;
            obj.status = this.status;
            obj.createdAt = this.createdAt;
            obj.balance = this.balance;
            return obj;
        }
    }
}
