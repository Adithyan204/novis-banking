# Novis API Specification

Base URL: `http://localhost:8080/api`

All responses follow the envelope format:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-09-23T10:00:00"
}
```

Authentication: `Authorization: Bearer <access_token>` (all endpoints except `/auth/**`)

---

## Auth

### POST /api/auth/register
Register a new user. Automatically creates a CHECKING account.

**Request:**
```json
{
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "password": "securepass123"
}
```
**Response:** `201 Created` — `{ "message": "User registered successfully" }`

---

### POST /api/auth/login
**Request:**
```json
{ "email": "jane@example.com", "password": "securepass123" }
```
**Response (MFA disabled):**
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "mfaRequired": false
}
```
**Response (MFA enabled):**
```json
{
  "mfaRequired": true,
  "mfaSessionToken": "eyJ..."
}
```

---

### POST /api/auth/mfa/verify
**Request:**
```json
{ "email": "jane@example.com", "otp": "123456" }
```
**Response:** Same as login success with full tokens.

> **Dev note:** The OTP is printed to the backend console log.

---

### POST /api/auth/refresh
**Request:** `{ "refreshToken": "eyJ..." }`
**Response:** `{ "accessToken": "eyJ...", "refreshToken": "eyJ..." }`

---

### POST /api/auth/logout *(requires auth)*
**Request:** `{ "refreshToken": "eyJ..." }`
**Response:** `200 OK`

---

## Accounts

### GET /api/accounts/me
Returns all accounts for the authenticated user with computed balances.

**Response:**
```json
[
  {
    "id": 1,
    "accountNumber": "1234567890",
    "accountType": "CHECKING",
    "currency": "USD",
    "status": "ACTIVE",
    "balance": 5000.00,
    "createdAt": "2026-01-01T10:00:00"
  }
]
```

### GET /api/accounts/{id}/transactions?page=0&size=20
Paginated transaction history for an account.

---

## Transfers

### POST /api/transfers
**Headers:** `Idempotency-Key: <uuid-v4>`

**Request:**
```json
{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 500.00,
  "currency": "USD",
  "description": "Rent payment"
}
```

**Response `201`:**
```json
{
  "id": 42,
  "idempotencyKey": "a1b2c3d4-...",
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 500.00,
  "currency": "USD",
  "status": "COMPLETED",
  "description": "Rent payment",
  "createdAt": "2026-09-23T10:15:00"
}
```

**Status values:**
- `COMPLETED` — ledger entries created, transfer done
- `FLAGGED` — held for fraud review, ledger NOT yet updated
- `FAILED` — rejected

**Errors:**
- `422` InsufficientFunds
- `403` KycNotVerified
- `409` DuplicateIdempotencyKey (returns original result)

### GET /api/transfers/{id}
Get a specific transfer by ID.

---

## KYC

### POST /api/kyc/documents
Upload a KYC document. Requires `multipart/form-data`.

**Form fields:**
- `documentType`: `PASSPORT | NATIONAL_ID | DRIVERS_LICENSE | UTILITY_BILL`
- `file`: the document file

**Response:** `201 Created`

### GET /api/kyc/status
```json
{
  "userId": 1,
  "kycStatus": "PENDING",
  "documents": [
    {
      "id": 1,
      "documentType": "PASSPORT",
      "status": "PENDING",
      "createdAt": "2026-09-23T09:00:00"
    }
  ]
}
```

---

## Admin *(ADMIN role required)*

### GET /api/admin/kyc/pending
All pending KYC documents for review.

### POST /api/admin/kyc/{id}/review
```json
{ "action": "APPROVED" }
// or
{ "action": "REJECTED", "rejectionReason": "Document is blurry" }
```

### GET /api/admin/fraud/flags
All fraud flags with `PENDING_REVIEW` status.

### POST /api/admin/fraud/flags/{id}/resolve
```json
{ "action": "APPROVED", "reviewNote": "Verified with customer" }
// APPROVED → creates ledger entries, sets transaction COMPLETED
// REJECTED → sets transaction FAILED
```

### GET /api/admin/reconciliation
Triggers reconciliation check and returns report:
```json
{
  "generatedAt": "2026-09-23T02:00:00",
  "accounts": [
    {
      "accountId": 1,
      "accountNumber": "1234567890",
      "computedBalance": 5000.00,
      "isConsistent": true
    }
  ]
}
```

### GET /api/admin/audit-logs?page=0&size=50
Paginated system-wide audit log.

### POST /api/admin/users/{id}/freeze
Freeze a user's account (sets status to FROZEN).
