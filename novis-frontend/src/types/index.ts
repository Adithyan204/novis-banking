export interface User {
  id: number;
  email: string;
  fullName: string;
  role: 'CUSTOMER' | 'ADMIN';
  kycStatus: string;
  mfaEnabled: boolean;
}

export interface Account {
  id: number;
  accountNumber: string;
  accountType: string;
  currency: string;
  status: string;
  balance: number;
  createdAt: string;
}

export interface Transaction {
  id: number;
  idempotencyKey: string;
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  currency: string;
  status: string;
  description: string;
  createdAt: string;
}

export interface FraudFlag {
  id: number;
  transactionId: number;
  ruleTriggered: string;
  severity: string;
  status: string;
  createdAt: string;
}

export interface KycDocument {
  id: number;
  documentType: string;
  status: string;
  rejectionReason?: string;
  createdAt: string;
}

export interface KycStatus {
  userId: number;
  kycStatus: string;
  documents: KycDocument[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  mfaRequired: boolean;
  mfaSessionToken?: string;
}
