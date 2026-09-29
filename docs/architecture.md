# Novis Architecture

## System Overview

```
┌─────────────────┐        ┌────────────────────────┐        ┌─────────────┐
│  React Frontend  │──────▶│  Spring Boot Backend    │──────▶│    MySQL    │
│ (Vite + TS)     │  REST/ │  (Modular Monolith)     │  JPA  │ (Ledger DB) │
└─────────────────┘  JSON  └────────────────────────┘        └─────────────┘
                                       │
                                       ▼
                            ┌──────────────────────┐
                            │   Scheduled Jobs      │
                            │ ReconciliationJob     │
                            │ (nightly @ 02:00)     │
                            └──────────────────────┘
```

## Module Dependency Graph

```
common (config, exceptions, response)
  └──▶ auth (UserDetails, JWT)
  └──▶ audit (AuditService)
  └──▶ account (Account, LedgerService)
        └──▶ transaction (TransferService, LedgerService)
              └──▶ fraud (FraudRuleEngine)
              └──▶ kyc (status check in transfers)
  └──▶ admin (cross-cutting: kyc, fraud, reconciliation)
  └──▶ scheduler (ReconciliationJob)
```

## Key Architectural Decisions

### 1. Immutable Ledger (Double-Entry Bookkeeping)

Account balances are **never stored as a mutable field**. Every financial movement creates an immutable `ledger_entry` row — a DEBIT from the sender and a CREDIT to the receiver. Balance is always derived:

```
balance = SUM(CREDIT entries) - SUM(DEBIT entries)
```

This eliminates an entire class of race-condition bugs. Two concurrent transfers cannot corrupt a balance because neither reads then writes a single field — both append to the ledger under pessimistic locking.

### 2. Pessimistic Locking on Transfer

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Account a WHERE a.id = :id")
Optional<Account> findByIdForUpdate(@Param("id") Long id);
```

`TransferService.transfer()` acquires a DB-level write lock on the sender account before reading its balance. Concurrent requests queue at the DB, not at the application layer.

### 3. Idempotency Keys

Every `POST /api/transfers` requires an `Idempotency-Key` header (UUID). The service checks whether the key already exists in `transactions.idempotency_key` (unique constraint). If it does, the original result is returned without re-processing. This handles client retries after network timeouts safely.

### 4. Fraud Rule Engine

A strategy pattern: each rule is a Spring bean implementing `FraudRule`:

| Rule | Trigger | Severity |
|------|---------|----------|
| `LargeAmountRule` | Amount > 3× 30-day average, or first transfer > $10,000 | HIGH / MEDIUM |
| `RapidTransferRule` | ≥ 5 transfers in 10 minutes from same account | HIGH |
| `NewBeneficiaryRule` | First-ever transfer to this destination AND amount > $5,000 | MEDIUM |

Flagged transactions are set to `FLAGGED` status and held in `fraud_flags` for admin review. The ledger entries are NOT created until an admin approves.

### 5. JWT Auth Flow

```
POST /api/auth/login
  ├─ MFA disabled → access_token (15 min) + refresh_token (7 days)
  └─ MFA enabled  → mfaRequired=true + mfaSessionToken (3 min)
                          │
                    POST /api/auth/mfa/verify
                          │
                    access_token + refresh_token

POST /api/auth/refresh
  └─ Validates refresh token against DB (revocable)
     → new access_token
```

### 6. KYC State Machine

```
PENDING → APPROVED ─┐
        → REJECTED  │─ admin reviews KycDocument
                    │
If all docs APPROVED → User.kycStatus = VERIFIED
If any doc REJECTED  → User.kycStatus = REJECTED
```

Transfers require `kycStatus == VERIFIED`. Without it, `403 KycNotVerifiedException`.

### 7. Nightly Reconciliation

`ReconciliationJob` runs at 02:00 AM daily. For each ACTIVE account, it:
1. Re-computes balance from `ledger_entries`
2. Logs the result to `audit_logs`
3. On mismatch: logs `RECONCILIATION_MISMATCH` — alerts the audit trail
