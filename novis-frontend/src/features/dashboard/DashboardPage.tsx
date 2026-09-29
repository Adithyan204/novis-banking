import React, { useEffect, useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { getMyAccounts } from '../../api/accounts';
import { Account } from '../../types';

// Inline safe AccountCard — avoids import chain crashes
const SafeAccountCard: React.FC<{ account: Account }> = ({ account }) => {
  const bal = typeof account.balance === 'number' ? account.balance.toFixed(2) : '0.00';
  const masked = account.accountNumber ? `•••• ${account.accountNumber.slice(-4)}` : '••••';
  return (
    <div className="bg-indigo-600 rounded-xl p-6 text-white shadow-lg relative overflow-hidden">
      <div className="flex justify-between items-start mb-6">
        <div>
          <p className="text-indigo-200 text-sm font-medium">{account.accountType} ACCOUNT</p>
          <p className="text-lg font-mono mt-1 tracking-widest">{masked}</p>
        </div>
        <span className={`text-xs px-2 py-1 rounded-full font-semibold ${account.status === 'ACTIVE' ? 'bg-green-400 text-green-900' : 'bg-red-400 text-red-900'}`}>
          {account.status}
        </span>
      </div>
      <div>
        <p className="text-indigo-200 text-sm">Available Balance</p>
        <p className="text-3xl font-bold mt-1">${bal} <span className="text-lg text-indigo-200">{account.currency}</span></p>
      </div>
    </div>
  );
};

export const DashboardPage = () => {
  const { user } = useAuth();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getMyAccounts()
      .then(res => {
        const data = res?.data?.data;
        setAccounts(Array.isArray(data) ? data : []);
      })
      .catch(err => {
        console.error(err);
        setError('Could not load accounts. Is the backend running?');
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-slate-900">
          Welcome back, {user?.fullName ?? 'User'} 👋
        </h1>
        <p className="text-slate-500 mt-1">Here's an overview of your accounts</p>
      </div>

      {/* KYC warning */}
      {user?.kycStatus !== 'VERIFIED' && (
        <div className="bg-amber-50 border border-amber-200 rounded-lg p-4 text-amber-800 text-sm">
          ⚠️ Your KYC status is <strong>{user?.kycStatus ?? 'PENDING'}</strong>. 
          You must complete KYC verification before you can make transfers.{' '}
          <a href="/kyc" className="underline font-medium">Upload documents →</a>
        </div>
      )}

      {/* Error */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-800 text-sm">
          ❌ {error}
        </div>
      )}

      {/* Accounts grid */}
      {accounts.length === 0 && !error ? (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 text-blue-800 text-sm">
          ℹ️ No accounts found. This shouldn't happen — check the backend is running.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {accounts.map(acc => (
            <SafeAccountCard key={acc.id} account={acc} />
          ))}
        </div>
      )}

      {/* Quick actions */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-4">
        <a href="/transfer" className="bg-white rounded-xl border border-slate-200 p-5 hover:shadow-md transition-shadow flex items-center gap-4">
          <div className="bg-indigo-100 rounded-lg p-3">
            <svg className="w-6 h-6 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
            </svg>
          </div>
          <div>
            <p className="font-semibold text-slate-900">Transfer</p>
            <p className="text-sm text-slate-500">Send money</p>
          </div>
        </a>
        <a href="/kyc" className="bg-white rounded-xl border border-slate-200 p-5 hover:shadow-md transition-shadow flex items-center gap-4">
          <div className="bg-green-100 rounded-lg p-3">
            <svg className="w-6 h-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <div>
            <p className="font-semibold text-slate-900">KYC</p>
            <p className="text-sm text-slate-500">Verify identity</p>
          </div>
        </a>
        {user?.role === 'ADMIN' && (
          <a href="/admin/fraud" className="bg-white rounded-xl border border-slate-200 p-5 hover:shadow-md transition-shadow flex items-center gap-4">
            <div className="bg-red-100 rounded-lg p-3">
              <svg className="w-6 h-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </div>
            <div>
              <p className="font-semibold text-slate-900">Fraud Flags</p>
              <p className="text-sm text-slate-500">Admin panel</p>
            </div>
          </a>
        )}
      </div>
    </div>
  );
};