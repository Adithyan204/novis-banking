import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { PrivateRoute } from './PrivateRoute';
import { AdminRoute } from './AdminRoute';
import { LoginPage } from '../features/auth/LoginPage';
import { RegisterPage } from '../features/auth/RegisterPage';
import { MfaPage } from '../features/auth/MfaPage';
import { DashboardPage } from '../features/dashboard/DashboardPage';
import { TransferPage } from '../features/transfer/TransferPage';
import { KycPage } from '../features/kyc/KycPage';
import { KycQueuePage } from '../features/admin/KycQueuePage';
import { FraudFlagsPage } from '../features/admin/FraudFlagsPage';
import { ReconciliationPage } from '../features/admin/ReconciliationPage';
import { ProfilePage } from '../features/profile/ProfilePage';
import { TransactionsPage } from '../features/transactions/TransactionsPage';
import { NotificationsPage } from '../features/notifications/NotificationsPage';

export const AppRouter = () => {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/mfa" element={<MfaPage />} />

      <Route element={<PrivateRoute />}>
        <Route path="/dashboard"     element={<DashboardPage />} />
        <Route path="/transfer"      element={<TransferPage />} />
        <Route path="/transactions"  element={<TransactionsPage />} />
        <Route path="/kyc"           element={<KycPage />} />
        <Route path="/profile"       element={<ProfilePage />} />
        <Route path="/notifications" element={<NotificationsPage />} />
      </Route>

      <Route element={<AdminRoute />}>
        <Route path="/admin/kyc"            element={<KycQueuePage />} />
        <Route path="/admin/fraud"          element={<FraudFlagsPage />} />
        <Route path="/admin/reconciliation" element={<ReconciliationPage />} />
      </Route>
    </Routes>
  );
};