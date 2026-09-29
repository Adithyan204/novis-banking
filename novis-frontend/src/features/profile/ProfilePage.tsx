import React, { useEffect, useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { getMyProfile, updateMyProfile, changePassword } from '../../api/users';
import { getMyAccounts } from '../../api/accounts';
import { Link } from 'react-router-dom';

const ROLE_COLORS: Record<string, string> = {
  ADMIN: 'bg-purple-100 text-purple-800',
  CUSTOMER: 'bg-blue-100 text-blue-800',
};

const KYC_COLORS: Record<string, string> = {
  VERIFIED: 'bg-green-100 text-green-800',
  PENDING: 'bg-amber-100 text-amber-800',
  REJECTED: 'bg-red-100 text-red-800',
};

const KYC_ICONS: Record<string, string> = {
  VERIFIED: '✅',
  PENDING: '⏳',
  REJECTED: '❌',
};

export const ProfilePage = () => {
  const { user, logout } = useAuth();
  const [profile, setProfile] = useState<any>(null);
  const [accounts, setAccounts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  // Edit name
  const [fullName, setFullName] = useState('');
  const [nameMsg, setNameMsg] = useState('');
  const [nameSaving, setNameSaving] = useState(false);

  // Change password
  const [pw, setPw] = useState({ current: '', next: '', confirm: '' });
  const [pwMsg, setPwMsg] = useState('');
  const [pwError, setPwError] = useState('');
  const [pwSaving, setPwSaving] = useState(false);

  useEffect(() => {
    Promise.all([getMyProfile(), getMyAccounts()])
      .then(([profileRes, accountsRes]) => {
        const p = profileRes.data.data;
        setProfile(p);
        setFullName(p.fullName);
        const accs = accountsRes?.data?.data;
        setAccounts(Array.isArray(accs) ? accs : []);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const handleNameSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setNameSaving(true);
    setNameMsg('');
    try {
      await updateMyProfile({ fullName });
      setNameMsg('✅ Name updated successfully!');
      setProfile((p: any) => ({ ...p, fullName }));
    } catch (err: any) {
      setNameMsg(err.response?.data?.message || 'Failed to update name');
    } finally {
      setNameSaving(false);
    }
  };

  const handlePwChange = async (e: React.FormEvent) => {
    e.preventDefault();
    setPwError('');
    setPwMsg('');
    if (pw.next !== pw.confirm) { setPwError('New passwords do not match'); return; }
    if (pw.next.length < 8) { setPwError('New password must be at least 8 characters'); return; }
    setPwSaving(true);
    try {
      await changePassword({ currentPassword: pw.current, newPassword: pw.next });
      setPwMsg('✅ Password changed! Signing you out in 3 seconds...');
      setPw({ current: '', next: '', confirm: '' });
      setTimeout(() => logout(), 3000);
    } catch (err: any) {
      setPwError(err.response?.data?.message || 'Failed to change password');
    } finally {
      setPwSaving(false);
    }
  };

  if (loading) return (
    <div className="flex items-center justify-center h-64">
      <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600" />
    </div>
  );

  const kycStatus = profile?.kycStatus ?? user?.kycStatus ?? 'PENDING';
  const role = profile?.role ?? user?.role ?? 'CUSTOMER';
  const nameStr = profile?.fullName ?? user?.fullName ?? 'User';
  const initials = nameStr.split(' ').map((w: string) => w[0]).join('').toUpperCase().slice(0, 2);
  const memberSince = profile?.createdAt
    ? new Date(profile.createdAt).toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
    : 'N/A';

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <h1 className="text-2xl font-bold text-slate-900">My Profile</h1>

      {/* Identity Card */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <div className="flex items-center gap-6">
          <div className="w-20 h-20 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white text-2xl font-bold shadow-lg flex-shrink-0">
            {initials}
          </div>
          <div className="flex-1 min-w-0">
            <h2 className="text-xl font-bold text-slate-900">{nameStr}</h2>
            <p className="text-slate-500 text-sm mt-0.5">{profile?.email ?? user?.email}</p>
            <div className="flex items-center gap-2 mt-2 flex-wrap">
              <span className={`text-xs px-2.5 py-0.5 rounded-full font-semibold ${ROLE_COLORS[role] || 'bg-slate-100 text-slate-700'}`}>{role}</span>
              <span className="text-xs text-slate-400">Member since {memberSince}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Account Details */}
      {accounts.length > 0 && (
        <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
          <h3 className="text-base font-semibold text-slate-900 mb-4">Linked Accounts</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {accounts.map(acc => (
              <div key={acc.id} className="bg-slate-50 rounded-lg p-4 border border-slate-100">
                <p className="text-xs text-slate-500 font-medium uppercase tracking-wider">{acc.accountType}</p>
                <p className="text-lg font-mono font-semibold text-slate-900 mt-1">•••• {acc.accountNumber?.slice(-4)}</p>
                <div className="flex items-center justify-between mt-2">
                  <span className={`text-xs px-2 py-0.5 rounded-full font-semibold ${acc.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>{acc.status}</span>
                  <span className="text-sm font-semibold text-slate-700">${(acc.balance ?? 0).toFixed(2)} {acc.currency}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* KYC Status */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <h3 className="text-base font-semibold text-slate-900 mb-4">KYC Verification</h3>
        <div className="flex items-center justify-between flex-wrap gap-4">
          <div className="flex items-center gap-3">
            <span className="text-2xl">{KYC_ICONS[kycStatus] || '❓'}</span>
            <div>
              <span className={`inline-block text-sm px-3 py-1 rounded-full font-semibold ${KYC_COLORS[kycStatus] || 'bg-slate-100 text-slate-700'}`}>{kycStatus}</span>
              <p className="text-xs text-slate-500 mt-1">
                {kycStatus === 'VERIFIED' && 'Your identity is verified. All features unlocked.'}
                {kycStatus === 'PENDING' && 'Documents under review. Transfers locked until verified.'}
                {kycStatus === 'REJECTED' && 'Documents rejected. Please resubmit.'}
              </p>
            </div>
          </div>
          {kycStatus !== 'VERIFIED' && (
            <Link to="/kyc" className="text-sm text-indigo-600 font-medium hover:text-indigo-800 underline whitespace-nowrap">Upload Documents →</Link>
          )}
        </div>
      </div>

      {/* Edit Name */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <h3 className="text-base font-semibold text-slate-900 mb-4">Edit Display Name</h3>
        <form onSubmit={handleNameSave} className="flex gap-3">
          <input type="text"
            className="flex-1 rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            value={fullName} onChange={e => setFullName(e.target.value)} required />
          <button type="submit" disabled={nameSaving}
            className="bg-indigo-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-indigo-700 disabled:opacity-50 whitespace-nowrap">
            {nameSaving ? 'Saving...' : 'Save Name'}
          </button>
        </form>
        {nameMsg && <p className={`text-sm mt-2 ${nameMsg.includes('✅') ? 'text-green-600' : 'text-red-600'}`}>{nameMsg}</p>}
      </div>

      {/* Change Password */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <h3 className="text-base font-semibold text-slate-900 mb-1">Change Password</h3>
        <p className="text-xs text-slate-500 mb-4">You will be signed out automatically after changing your password.</p>
        <form onSubmit={handlePwChange} className="space-y-3">
          {pwError && <div className="bg-red-50 border border-red-200 rounded-lg p-3 text-red-700 text-sm">{pwError}</div>}
          {pwMsg && <div className="bg-green-50 border border-green-200 rounded-lg p-3 text-green-700 text-sm">{pwMsg}</div>}
          <input type="password" placeholder="Current password" required
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            value={pw.current} onChange={e => setPw(p => ({ ...p, current: e.target.value }))} />
          <input type="password" placeholder="New password (min 8 chars)" required
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            value={pw.next} onChange={e => setPw(p => ({ ...p, next: e.target.value }))} />
          <input type="password" placeholder="Confirm new password" required
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            value={pw.confirm} onChange={e => setPw(p => ({ ...p, confirm: e.target.value }))} />
          <button type="submit" disabled={pwSaving}
            className="bg-red-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-red-700 disabled:opacity-50 w-full">
            {pwSaving ? 'Changing...' : 'Change Password'}
          </button>
        </form>
      </div>
    </div>
  );
};
