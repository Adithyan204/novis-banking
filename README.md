# Novis — Fraud-Aware Digital Banking Platform

> A full-stack banking web application that models how real financial systems work — immutable ledger, idempotent transactions, rule-based fraud detection, and a full audit trail.

![Stack](https://img.shields.io/badge/Backend-Spring%20Boot%203.3-green?style=flat-square&logo=spring)
![Stack](https://img.shields.io/badge/Frontend-React%2018%20%2B%20TypeScript-blue?style=flat-square&logo=react)
![Stack](https://img.shields.io/badge/Auth-JWT%20%2B%20MFA-orange?style=flat-square)
![Stack](https://img.shields.io/badge/Database-H2%20%2F%20MySQL-red?style=flat-square)

---

## Why This Project Exists

Most student banking projects treat balance as a mutable number and transfers as a single database update. Novis models banking the way real financial systems do:

| Problem | Novis Solution |
|---------|---------------|
| Race conditions on concurrent transfers | Pessimistic locking (`SELECT FOR UPDATE`) |
| Duplicate charges from retried requests | Idempotency key on every transfer |
| No audit trail | Immutable `audit_logs` table, every action recorded |
| Weak fraud detection | Rule engine: LargeAmount, RapidTransfer, NewBeneficiary rules |
| Balance inconsistency | Balance computed as `SUM(CREDIT) - SUM(DEBIT)` from ledger — never stored |

---

## Features

### Banking Core
- ✅ **Immutable double-entry ledger** — balance is always computed, never stored directly
- ✅ **Idempotent transfers** — duplicate requests return the original result, not a duplicate charge
- ✅ **Pessimistic locking** — concurrent transfers on the same account are serialised
- ✅ **Multi-currency transfers** — USD, EUR, GBP, INR, JPY, AED, SGD, CAD, AUD with live rate preview

### Security & Identity
- ✅ **JWT authentication** — 15-min access tokens, 7-day refresh tokens, stored in localStorage
- ✅ **MFA (TOTP-simulated)** — 6-digit OTP generated on login, logged to console for dev
- ✅ **KYC document upload** — Passport, National ID, Driver's License, Utility Bill
- ✅ **Role-based access** — `CUSTOMER` and `ADMIN` roles

### Fraud Detection
- ✅ **LargeAmountRule** — flags transfers >3× the account's 30-day average
- ✅ **RapidTransferRule** — flags ≥5 transfers from the same account within 10 minutes
- ✅ **NewBeneficiaryRule** — flags first-time transfers >$5,000 to a new recipient

### Admin Panel
- ✅ KYC review queue (approve / reject with reason)
- ✅ Fraud flag management (approve / reject flagged transactions)
- ✅ Daily reconciliation job (2 AM cron, compares ledger balances)
- ✅ Full audit log viewer

### Frontend Pages
| Page | Path |
|------|------|
| Dashboard | `/dashboard` |
| Transfer Funds | `/transfer` |
| Transaction History | `/transactions` |
| KYC Verification | `/kyc` |
| Profile | `/profile` |
| Activity Feed | `/notifications` |
| Admin — KYC Queue | `/admin/kyc` |
| Admin — Fraud Flags | `/admin/fraud` |
| Admin — Reconciliation | `/admin/reconciliation` |

---

## Tech Stack

### Backend
- **Java 21** · **Spring Boot 3.3**
- **Spring Security 6** (JWT stateless)
- **Spring Data JPA** + Hibernate
- **Flyway** migrations (V1–V4)
- **H2** (dev, in-memory) / **MySQL** (prod)
- **jjwt 0.12** for JWT signing

### Frontend
- **React 18** + **TypeScript**
- **Vite 5**
- **Tailwind CSS 3**
- **React Router 6**
- **Axios** with request/response interceptors
- **Recharts** for balance chart
- **lucide-react** icons

---

## Running Locally

### Prerequisites
- Java 21+
- Maven 3.8+ (`brew install maven`)
- Node 18+ and npm

### Backend
```bash
cd novis-backend
mvn spring-boot:run
# Starts on http://localhost:8080
# H2 console: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:novisdb | User: sa | Password: (empty)
```

### Frontend
```bash
cd novis-frontend
npm install
npm run dev
# Starts on http://localhost:5173
```

### Demo Accounts
Register via the UI, then use H2 console to promote:
```sql
UPDATE users SET role='ADMIN', kyc_status='VERIFIED' WHERE email='admin@example.com';
UPDATE users SET kyc_status='VERIFIED' WHERE email='user@example.com';
```

---

## Architecture

```
novis-banking/
├── novis-backend/           # Spring Boot API
│   ├── auth/                # Register, login, MFA, JWT
│   ├── account/             # Account management
│   ├── transaction/         # Transfer, ledger, idempotency
│   ├── fraud/               # Rule engine + flag management
│   ├── kyc/                 # Document upload & review
│   ├── audit/               # Immutable audit trail
│   ├── admin/               # Admin-only endpoints
│   └── scheduler/           # Reconciliation cron job
│
├── novis-frontend/          # React + TypeScript SPA
│   ├── features/            # Page-level components
│   ├── api/                 # Axios API clients
│   ├── components/          # Shared UI components
│   └── context/             # Auth context + hooks
│
└── docker-compose.yml       # MySQL + backend + frontend
```

---

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | Public | Create account |
| POST | `/api/auth/login` | Public | Login (returns JWT or MFA token) |
| POST | `/api/auth/mfa/verify` | Public | Verify OTP |
| POST | `/api/auth/refresh` | Public | Refresh access token |
| GET | `/api/accounts/me` | JWT | List user's accounts |
| POST | `/api/transfers` | JWT + `Idempotency-Key` header | Create transfer |
| GET | `/api/users/me` | JWT | Get profile |
| POST | `/api/users/me/change-password` | JWT | Change password |
| GET | `/api/kyc/status` | JWT | KYC status |
| POST | `/api/kyc/documents` | JWT | Upload document |
| GET | `/api/admin/fraud/flags` | ADMIN | List fraud flags |
| POST | `/api/admin/fraud/flags/{id}/resolve` | ADMIN | Approve/reject flag |

---

## Docker

```bash
docker-compose up --build
# Frontend: http://localhost:5173
# Backend:  http://localhost:8080
# MySQL:    localhost:3306
```

---

*Built to demonstrate production-grade backend engineering patterns in a banking context.*
