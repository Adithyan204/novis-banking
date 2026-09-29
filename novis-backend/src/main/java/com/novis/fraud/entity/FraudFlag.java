package com.novis.fraud.entity;

import com.novis.auth.entity.User;
import com.novis.transaction.entity.Transaction;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_flags")
public class FraudFlag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(name = "rule_triggered", nullable = false)
    private String ruleTriggered;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
        private Severity severity = Severity.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
        private FlagStatus status = FlagStatus.PENDING_REVIEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public enum Severity {
        LOW, MEDIUM, HIGH
    }

    public enum FlagStatus {
        PENDING_REVIEW, APPROVED, REJECTED
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getRuleTriggered() {
        return ruleTriggered;
    }

    public void setRuleTriggered(String ruleTriggered) {
        this.ruleTriggered = ruleTriggered;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public FlagStatus getStatus() {
        return status;
    }

    public void setStatus(FlagStatus status) {
        this.status = status;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public FraudFlag() {
        this.severity = Severity.MEDIUM;
        this.status = FlagStatus.PENDING_REVIEW;
        this.createdAt = LocalDateTime.now();
    }

    public FraudFlag(Long id, Transaction transaction, String ruleTriggered, Severity severity, FlagStatus status, User reviewedBy, String reviewNote, LocalDateTime createdAt, LocalDateTime reviewedAt) {
        this.id = id;
        this.transaction = transaction;
        this.ruleTriggered = ruleTriggered;
        this.severity = severity;
        this.status = status;
        this.reviewedBy = reviewedBy;
        this.reviewNote = reviewNote;
        this.createdAt = createdAt;
        this.reviewedAt = reviewedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Transaction transaction;
        private String ruleTriggered;
        private Severity severity = Severity.MEDIUM;
        private FlagStatus status = FlagStatus.PENDING_REVIEW;
        private User reviewedBy;
        private String reviewNote;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime reviewedAt;
        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder transaction(Transaction transaction) {
            this.transaction = transaction;
            return this;
        }

        public Builder ruleTriggered(String ruleTriggered) {
            this.ruleTriggered = ruleTriggered;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder status(FlagStatus status) {
            this.status = status;
            return this;
        }

        public Builder reviewedBy(User reviewedBy) {
            this.reviewedBy = reviewedBy;
            return this;
        }

        public Builder reviewNote(String reviewNote) {
            this.reviewNote = reviewNote;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder reviewedAt(LocalDateTime reviewedAt) {
            this.reviewedAt = reviewedAt;
            return this;
        }

        public FraudFlag build() {
            FraudFlag obj = new FraudFlag();
            obj.id = this.id;
            obj.transaction = this.transaction;
            obj.ruleTriggered = this.ruleTriggered;
            obj.severity = this.severity;
            obj.status = this.status;
            obj.reviewedBy = this.reviewedBy;
            obj.reviewNote = this.reviewNote;
            obj.createdAt = this.createdAt;
            obj.reviewedAt = this.reviewedAt;
            return obj;
        }
    }
}
