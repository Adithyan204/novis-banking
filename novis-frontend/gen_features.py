import os

base_dir = "/Users/adithyanmenont/Anti gravity/Bank_service/novis-frontend"

files = {
    "src/features/auth/LoginPage.tsx": """import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login as loginApi } from '../../api/auth';
import { useAuth } from '../../hooks/useAuth';
import { Button } from '../../components/Button';
import { Input } from '../../components/Input';
import { Alert } from '../../components/Alert';
import { Shield } from 'lucide-react';

export const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await loginApi({ email, password });
      const { data } = res.data;
      if (data.mfaRequired) {
        navigate('/mfa', { state: { mfaSessionToken: data.mfaSessionToken, email } });
      } else {
        login(data.accessToken, data.refreshToken);
        navigate('/dashboard');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8 bg-white p-8 rounded-xl shadow-sm border border-slate-100">
        <div className="text-center">
          <Shield className="mx-auto h-12 w-12 text-indigo-600" />
          <h2 className="mt-6 text-3xl font-extrabold text-slate-900">Sign in to Novis</h2>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          {error && <Alert variant="danger">{error}</Alert>}
          <div className="space-y-4">
            <Input label="Email address" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
            <Input label="Password" type="password" required value={password} onChange={(e) => setPassword(e.target.value)} />
          </div>
          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign in'}
          </Button>
          <div className="text-sm text-center">
            <Link to="/register" className="font-medium text-indigo-600 hover:text-indigo-500">
              Don't have an account? Register here
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
};""",

    "src/features/auth/RegisterPage.tsx": """import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register } from '../../api/auth';
import { Button } from '../../components/Button';
import { Input } from '../../components/Input';
import { Alert } from '../../components/Alert';
import { Shield } from 'lucide-react';

export const RegisterPage = () => {
  const [formData, setFormData] = useState({ fullName: '', email: '', password: '', confirmPassword: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (formData.password !== formData.confirmPassword) {
      return setError('Passwords do not match');
    }
    if (formData.password.length < 8) {
      return setError('Password must be at least 8 characters');
    }
    setError('');
    setLoading(true);
    try {
      await register({ fullName: formData.fullName, email: formData.email, password: formData.password });
      navigate('/login');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 py-12 px-4">
      <div className="max-w-md w-full space-y-8 bg-white p-8 rounded-xl shadow-sm border border-slate-100">
        <div className="text-center">
          <Shield className="mx-auto h-12 w-12 text-indigo-600" />
          <h2 className="mt-6 text-3xl font-extrabold text-slate-900">Create your account</h2>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          {error && <Alert variant="danger">{error}</Alert>}
          <div className="space-y-4">
            <Input label="Full Name" required value={formData.fullName} onChange={(e) => setFormData({...formData, fullName: e.target.value})} />
            <Input label="Email address" type="email" required value={formData.email} onChange={(e) => setFormData({...formData, email: e.target.value})} />
            <Input label="Password" type="password" required value={formData.password} onChange={(e) => setFormData({...formData, password: e.target.value})} />
            <Input label="Confirm Password" type="password" required value={formData.confirmPassword} onChange={(e) => setFormData({...formData, confirmPassword: e.target.value})} />
          </div>
          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Registering...' : 'Register'}
          </Button>
          <div className="text-sm text-center">
            <Link to="/login" className="font-medium text-indigo-600 hover:text-indigo-500">
              Already have an account? Sign in
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
};""",

    "src/features/auth/MfaPage.tsx": """import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { verifyMfa } from '../../api/auth';
import { useAuth } from '../../hooks/useAuth';
import { Button } from '../../components/Button';
import { Input } from '../../components/Input';
import { Alert } from '../../components/Alert';
import { Shield } from 'lucide-react';

export const MfaPage = () => {
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  
  const mfaSessionToken = location.state?.mfaSessionToken;
  const email = location.state?.email;

  if (!mfaSessionToken) {
    navigate('/login');
    return null;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await verifyMfa({ email, mfaSessionToken, code });
      const { data } = res.data;
      login(data.accessToken, data.refreshToken);
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.message || 'MFA verification failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 py-12 px-4">
      <div className="max-w-md w-full space-y-8 bg-white p-8 rounded-xl shadow-sm border border-slate-100">
        <div className="text-center">
          <Shield className="mx-auto h-12 w-12 text-indigo-600" />
          <h2 className="mt-6 text-3xl font-extrabold text-slate-900">Two-Factor Authentication</h2>
          <p className="mt-2 text-sm text-slate-600">Check the server console for your OTP code</p>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          {error && <Alert variant="danger">{error}</Alert>}
          <div className="space-y-4">
            <Input label="6-digit Code" required maxLength={6} value={code} onChange={(e) => setCode(e.target.value)} />
          </div>
          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Verifying...' : 'Verify'}
          </Button>
        </form>
      </div>
    </div>
  );
};""",

    "src/features/dashboard/DashboardPage.tsx": """import React, { useEffect, useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { getMyAccounts } from '../../api/accounts';
import { Account } from '../../types';
import { AccountCard } from './AccountCard';
import { TransactionList } from './TransactionList';
import { BalanceChart } from './BalanceChart';
import { Alert } from '../../components/Alert';
import { Spinner } from '../../components/Spinner';

export const DashboardPage = () => {
  const { user } = useAuth();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchAccounts = async () => {
      try {
        const res = await getMyAccounts();
        setAccounts(res.data.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchAccounts();
  }, []);

  if (loading) return <Spinner />;

  const primaryAccount = accounts.length > 0 ? accounts[0] : null;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-slate-900">Welcome back, {user?.fullName}</h1>
      </div>

      {user?.kycStatus !== 'APPROVED' && (
        <Alert variant="warning">
          Your KYC status is currently <strong>{user?.kycStatus}</strong>. Please complete KYC to unlock full account features.
        </Alert>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {accounts.map(acc => (
          <AccountCard key={acc.id} account={acc} />
        ))}
        {accounts.length === 0 && (
          <div className="col-span-3">
            <Alert variant="info">You don't have any accounts yet.</Alert>
          </div>
        )}
      </div>

      {primaryAccount && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-6">
            <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
              <h2 className="text-lg font-semibold mb-4">Recent Transactions</h2>
              <TransactionList accountId={primaryAccount.id} />
            </div>
          </div>
          <div className="lg:col-span-1 space-y-6">
            <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200 h-96">
              <h2 className="text-lg font-semibold mb-4">Balance History</h2>
              <BalanceChart />
            </div>
          </div>
        </div>
      )}
    </div>
  );
};""",

    "src/features/dashboard/AccountCard.tsx": """import React from 'react';
import { Account } from '../../types';
import { Badge } from '../../components/Badge';
import { CreditCard } from 'lucide-react';

export const AccountCard: React.FC<{ account: Account }> = ({ account }) => {
  const maskedNumber = `•••• ${account.accountNumber.slice(-4)}`;
  
  return (
    <div className="bg-indigo-600 rounded-xl p-6 text-white shadow-lg relative overflow-hidden">
      <div className="absolute top-0 right-0 -mt-4 -mr-4 w-24 h-24 bg-indigo-500 rounded-full opacity-50 blur-xl"></div>
      
      <div className="flex justify-between items-start mb-8 relative z-10">
        <div>
          <p className="text-indigo-200 text-sm font-medium">{account.accountType} ACCOUNT</p>
          <p className="text-xl font-mono mt-1 tracking-widest">{maskedNumber}</p>
        </div>
        <CreditCard className="w-8 h-8 text-indigo-300" />
      </div>

      <div className="relative z-10">
        <p className="text-indigo-200 text-sm">Available Balance</p>
        <div className="flex items-baseline space-x-2">
          <span className="text-3xl font-bold">${account.balance.toFixed(2)}</span>
          <span className="text-indigo-200 font-medium">{account.currency}</span>
        </div>
      </div>

      <div className="mt-6 flex justify-between items-center relative z-10">
        <Badge variant={account.status === 'ACTIVE' ? 'success' : 'danger'}>
          {account.status}
        </Badge>
      </div>
    </div>
  );
};""",

    "src/features/dashboard/TransactionList.tsx": """import React, { useEffect, useState } from 'react';
import { getAccountTransactions } from '../../api/accounts';
import { Transaction } from '../../types';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/Table';
import { Badge } from '../../components/Badge';
import { Spinner } from '../../components/Spinner';

export const TransactionList: React.FC<{ accountId: number }> = ({ accountId }) => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchTx = async () => {
      try {
        const res = await getAccountTransactions(accountId);
        setTransactions(res.data.data.content);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchTx();
  }, [accountId]);

  if (loading) return <Spinner />;

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Date</TableHead>
          <TableHead>Description</TableHead>
          <TableHead>Amount</TableHead>
          <TableHead>Status</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {transactions.map(tx => {
          const isCredit = tx.toAccountId === accountId;
          return (
            <TableRow key={tx.id}>
              <TableCell className="text-slate-500">{new Date(tx.createdAt).toLocaleDateString()}</TableCell>
              <TableCell className="font-medium">{tx.description}</TableCell>
              <TableCell className={isCredit ? 'text-green-600 font-semibold' : 'text-slate-900 font-semibold'}>
                {isCredit ? '+' : '-'}${tx.amount.toFixed(2)}
              </TableCell>
              <TableCell>
                <Badge variant={
                  tx.status === 'COMPLETED' ? 'success' :
                  tx.status === 'PENDING' ? 'warning' :
                  tx.status === 'FLAGGED' ? 'danger' : 'default'
                }>{tx.status}</Badge>
              </TableCell>
            </TableRow>
          );
        })}
        {transactions.length === 0 && (
          <TableRow>
            <TableCell colSpan={4} className="text-center text-slate-500">No transactions found</TableCell>
          </TableRow>
        )}
      </TableBody>
    </Table>
  );
};""",

    "src/features/dashboard/BalanceChart.tsx": """import React from 'react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const data = [
  { name: 'Mon', balance: 4000 },
  { name: 'Tue', balance: 3000 },
  { name: 'Wed', balance: 2000 },
  { name: 'Thu', balance: 2780 },
  { name: 'Fri', balance: 1890 },
  { name: 'Sat', balance: 2390 },
  { name: 'Sun', balance: 3490 },
];

export const BalanceChart: React.FC = () => {
  return (
    <ResponsiveContainer width="100%" height="100%">
      <AreaChart data={data} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
        <defs>
          <linearGradient id="colorBalance" x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%" stopColor="#4f46e5" stopOpacity={0.8}/>
            <stop offset="95%" stopColor="#4f46e5" stopOpacity={0}/>
          </linearGradient>
        </defs>
        <XAxis dataKey="name" stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
        <YAxis stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} tickFormatter={(value) => `$${value}`} />
        <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
        <Tooltip contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }} />
        <Area type="monotone" dataKey="balance" stroke="#4f46e5" fillOpacity={1} fill="url(#colorBalance)" />
      </AreaChart>
    </ResponsiveContainer>
  );
};""",

    "src/features/transfer/TransferPage.tsx": """import React, { useState, useEffect } from 'react';
import { getMyAccounts } from '../../api/accounts';
import { createTransfer } from '../../api/transfers';
import { Account } from '../../types';
import { Button } from '../../components/Button';
import { Input } from '../../components/Input';
import { Alert } from '../../components/Alert';

export const TransferPage = () => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [formData, setFormData] = useState({
    fromAccountId: '',
    toAccountNumber: '',
    amount: '',
    description: ''
  });
  const [idempotencyKey, setIdempotencyKey] = useState(crypto.randomUUID());
  const [status, setStatus] = useState<{type: 'success' | 'error' | 'warning', message: string} | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    getMyAccounts().then(res => {
      setAccounts(res.data.data);
      if (res.data.data.length > 0) {
        setFormData(prev => ({ ...prev, fromAccountId: res.data.data[0].id.toString() }));
      }
    });
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setStatus(null);
    try {
      const payload = {
        fromAccountId: Number(formData.fromAccountId),
        toAccountNumber: formData.toAccountNumber,
        amount: Number(formData.amount),
        currency: 'USD',
        description: formData.description
      };
      const res = await createTransfer(payload, idempotencyKey);
      const tx = res.data.data;
      if (tx.status === 'FLAGGED') {
        setStatus({ type: 'warning', message: `Transfer is under fraud review (ID: ${tx.id})` });
      } else {
        setStatus({ type: 'success', message: `Transfer completed successfully (ID: ${tx.id})` });
      }
      setFormData({ ...formData, toAccountNumber: '', amount: '', description: '' });
      setIdempotencyKey(crypto.randomUUID());
    } catch (err: any) {
      setStatus({ type: 'error', message: err.response?.data?.message || 'Transfer failed' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <h1 className="text-2xl font-bold text-slate-900">Transfer Funds</h1>
      
      <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
        {status && (
          <Alert variant={status.type === 'error' ? 'danger' : status.type} className="mb-6">
            {status.message}
          </Alert>
        )}
        
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">From Account</label>
            <select
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              value={formData.fromAccountId}
              onChange={e => setFormData({...formData, fromAccountId: e.target.value})}
              required
            >
              {accounts.map(acc => (
                <option key={acc.id} value={acc.id}>
                  {acc.accountType} - {acc.accountNumber.slice(-4)} (${acc.balance.toFixed(2)})
                </option>
              ))}
            </select>
          </div>
          
          <Input 
            label="To Account Number" 
            required 
            value={formData.toAccountNumber} 
            onChange={e => setFormData({...formData, toAccountNumber: e.target.value})} 
          />
          
          <Input 
            label="Amount (USD)" 
            type="number" 
            step="0.01" 
            min="0.01"
            required 
            value={formData.amount} 
            onChange={e => setFormData({...formData, amount: e.target.value})} 
          />
          
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Description</label>
            <textarea
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              rows={3}
              value={formData.description}
              onChange={e => setFormData({...formData, description: e.target.value})}
              required
            />
          </div>

          <div className="flex gap-4 items-end">
            <div className="flex-1">
              <Input label="Idempotency Key (Auto-generated)" value={idempotencyKey} readOnly className="bg-slate-50 text-slate-500" />
            </div>
            <Button type="button" variant="secondary" onClick={() => setIdempotencyKey(crypto.randomUUID())}>
              Regenerate
            </Button>
          </div>

          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? 'Processing...' : 'Send Transfer'}
          </Button>
        </form>
      </div>
    </div>
  );
};""",

    "src/features/kyc/KycPage.tsx": """import React, { useEffect, useState } from 'react';
import { getKycStatus, uploadDocument } from '../../api/kyc';
import { KycStatus } from '../../types';
import { Button } from '../../components/Button';
import { Badge } from '../../components/Badge';
import { Alert } from '../../components/Alert';
import { Spinner } from '../../components/Spinner';

export const KycPage = () => {
  const [status, setStatus] = useState<KycStatus | null>(null);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [docType, setDocType] = useState('PASSPORT');
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState('');

  const fetchStatus = async () => {
    try {
      const res = await getKycStatus();
      setStatus(res.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStatus();
  }, []);

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file) return;
    setUploading(true);
    setError('');
    try {
      await uploadDocument(docType, file);
      setFile(null);
      await fetchStatus();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  if (loading) return <Spinner />;

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      <h1 className="text-2xl font-bold text-slate-900">KYC Verification</h1>

      <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
        <h2 className="text-lg font-semibold mb-4">Current Status</h2>
        <Badge variant={
          status?.kycStatus === 'APPROVED' ? 'success' : 
          status?.kycStatus === 'REJECTED' ? 'danger' : 
          status?.kycStatus === 'PENDING' ? 'warning' : 'default'
        } className="text-sm px-4 py-1">
          {status?.kycStatus || 'NONE'}
        </Badge>
      </div>

      <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
        <h2 className="text-lg font-semibold mb-4">Upload Document</h2>
        {error && <Alert variant="danger" className="mb-4">{error}</Alert>}
        <form onSubmit={handleUpload} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Document Type</label>
            <select
              className="w-full max-w-sm rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              value={docType}
              onChange={e => setDocType(e.target.value)}
            >
              <option value="PASSPORT">Passport</option>
              <option value="NATIONAL_ID">National ID</option>
              <option value="DRIVERS_LICENSE">Driver's License</option>
              <option value="UTILITY_BILL">Utility Bill</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">File</label>
            <input 
              type="file" 
              onChange={e => setFile(e.target.files?.[0] || null)}
              className="block w-full text-sm text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100"
              required
            />
          </div>
          <Button type="submit" disabled={!file || uploading}>
            {uploading ? 'Uploading...' : 'Upload Document'}
          </Button>
        </form>
      </div>

      <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
        <h2 className="text-lg font-semibold mb-4">Uploaded Documents</h2>
        {status?.documents.length === 0 ? (
          <p className="text-slate-500">No documents uploaded yet.</p>
        ) : (
          <div className="space-y-4">
            {status?.documents.map(doc => (
              <div key={doc.id} className="flex justify-between items-center p-4 border border-slate-100 rounded-lg bg-slate-50">
                <div>
                  <p className="font-medium text-slate-900">{doc.documentType}</p>
                  <p className="text-xs text-slate-500">{new Date(doc.createdAt).toLocaleString()}</p>
                  {doc.rejectionReason && <p className="text-sm text-red-600 mt-1">Reason: {doc.rejectionReason}</p>}
                </div>
                <Badge variant={
                  doc.status === 'APPROVED' ? 'success' : 
                  doc.status === 'REJECTED' ? 'danger' : 'warning'
                }>{doc.status}</Badge>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};""",

    "src/features/admin/KycQueuePage.tsx": """import React, { useEffect, useState } from 'react';
import { getPendingKyc, reviewKyc } from '../../api/admin';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/Table';
import { Button } from '../../components/Button';
import { Modal } from '../../components/Modal';
import { Input } from '../../components/Input';
import { Spinner } from '../../components/Spinner';

export const KycQueuePage = () => {
  const [documents, setDocuments] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedDoc, setSelectedDoc] = useState<number | null>(null);
  const [rejectReason, setRejectReason] = useState('');

  const fetchDocs = async () => {
    try {
      const res = await getPendingKyc();
      setDocuments(res.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDocs();
  }, []);

  const handleAction = async (id: number, action: string, reason?: string) => {
    try {
      await reviewKyc(id, { action, rejectionReason: reason });
      setSelectedDoc(null);
      setRejectReason('');
      fetchDocs();
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) return <Spinner />;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-slate-900">KYC Review Queue</h1>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>User ID</TableHead>
            <TableHead>Document Type</TableHead>
            <TableHead>Uploaded At</TableHead>
            <TableHead>Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {documents.map(doc => (
            <TableRow key={doc.id}>
              <TableCell>{doc.userId}</TableCell>
              <TableCell>{doc.documentType}</TableCell>
              <TableCell>{new Date(doc.createdAt).toLocaleString()}</TableCell>
              <TableCell className="space-x-2">
                <Button size="sm" variant="primary" onClick={() => handleAction(doc.id, 'APPROVE')}>Approve</Button>
                <Button size="sm" variant="danger" onClick={() => setSelectedDoc(doc.id)}>Reject</Button>
              </TableCell>
            </TableRow>
          ))}
          {documents.length === 0 && (
            <TableRow>
              <TableCell colSpan={4} className="text-center text-slate-500">No pending documents</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>

      <Modal isOpen={!!selectedDoc} onClose={() => setSelectedDoc(null)} title="Reject KYC Document">
        <div className="space-y-4">
          <Input 
            label="Rejection Reason" 
            value={rejectReason} 
            onChange={e => setRejectReason(e.target.value)} 
            placeholder="e.g. Image blurry"
          />
          <div className="flex justify-end space-x-2">
            <Button variant="secondary" onClick={() => setSelectedDoc(null)}>Cancel</Button>
            <Button variant="danger" onClick={() => handleAction(selectedDoc!, 'REJECT', rejectReason)} disabled={!rejectReason}>Reject Document</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};""",

    "src/features/admin/FraudFlagsPage.tsx": """import React, { useEffect, useState } from 'react';
import { getFraudFlags, resolveFlag } from '../../api/admin';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/Table';
import { Button } from '../../components/Button';
import { Badge } from '../../components/Badge';
import { Modal } from '../../components/Modal';
import { Input } from '../../components/Input';
import { Spinner } from '../../components/Spinner';

export const FraudFlagsPage = () => {
  const [flags, setFlags] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedFlag, setSelectedFlag] = useState<number | null>(null);
  const [actionType, setActionType] = useState<'APPROVE' | 'REJECT' | null>(null);
  const [reviewNote, setReviewNote] = useState('');

  const fetchFlags = async () => {
    try {
      const res = await getFraudFlags();
      setFlags(res.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFlags();
  }, []);

  const handleResolve = async () => {
    if (!selectedFlag || !actionType) return;
    try {
      await resolveFlag(selectedFlag, { action: actionType, reviewNote });
      setSelectedFlag(null);
      setReviewNote('');
      fetchFlags();
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) return <Spinner />;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-slate-900">Fraud Flags Review</h1>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Tx ID</TableHead>
            <TableHead>Rule Triggered</TableHead>
            <TableHead>Severity</TableHead>
            <TableHead>Created At</TableHead>
            <TableHead>Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {flags.map(flag => (
            <TableRow key={flag.id}>
              <TableCell>{flag.transactionId}</TableCell>
              <TableCell className="font-medium">{flag.ruleTriggered}</TableCell>
              <TableCell>
                <Badge variant={
                  flag.severity === 'HIGH' ? 'danger' : 
                  flag.severity === 'MEDIUM' ? 'warning' : 'info'
                }>{flag.severity}</Badge>
              </TableCell>
              <TableCell>{new Date(flag.createdAt).toLocaleString()}</TableCell>
              <TableCell className="space-x-2">
                <Button size="sm" variant="primary" onClick={() => { setSelectedFlag(flag.id); setActionType('APPROVE'); }}>Approve Tx</Button>
                <Button size="sm" variant="danger" onClick={() => { setSelectedFlag(flag.id); setActionType('REJECT'); }}>Reject Tx</Button>
              </TableCell>
            </TableRow>
          ))}
          {flags.length === 0 && (
            <TableRow>
              <TableCell colSpan={5} className="text-center text-slate-500">No pending fraud flags</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>

      <Modal isOpen={!!selectedFlag} onClose={() => setSelectedFlag(null)} title={`Confirm ${actionType === 'APPROVE' ? 'Approval' : 'Rejection'}`}>
        <div className="space-y-4">
          <Input 
            label="Review Note (Optional)" 
            value={reviewNote} 
            onChange={e => setReviewNote(e.target.value)} 
          />
          <div className="flex justify-end space-x-2">
            <Button variant="secondary" onClick={() => setSelectedFlag(null)}>Cancel</Button>
            <Button variant={actionType === 'APPROVE' ? 'primary' : 'danger'} onClick={handleResolve}>
              Confirm
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};""",

    "src/features/admin/ReconciliationPage.tsx": """import React, { useState } from 'react';
import { getReconciliation } from '../../api/admin';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/Table';
import { Button } from '../../components/Button';
import { Alert } from '../../components/Alert';
import { CheckCircle2, XCircle } from 'lucide-react';

export const ReconciliationPage = () => {
  const [results, setResults] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const runReconciliation = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await getReconciliation();
      setResults(res.data.data.discrepancies || []);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to run reconciliation');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-slate-900">Ledger Reconciliation</h1>
        <Button onClick={runReconciliation} disabled={loading}>
          {loading ? 'Running...' : 'Run Reconciliation Now'}
        </Button>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}

      <div className="bg-white rounded-xl shadow-sm border border-slate-200">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Account Number</TableHead>
              <TableHead>Recorded Balance</TableHead>
              <TableHead>Computed Balance</TableHead>
              <TableHead>Status</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {results.map((res: any, idx: number) => (
              <TableRow key={idx}>
                <TableCell className="font-mono">{res.accountNumber}</TableCell>
                <TableCell>${res.recordedBalance?.toFixed(2)}</TableCell>
                <TableCell>${res.computedBalance?.toFixed(2)}</TableCell>
                <TableCell>
                  {res.isConsistent ? 
                    <span className="flex items-center text-green-600"><CheckCircle2 className="w-5 h-5 mr-1"/> Consistent</span> : 
                    <span className="flex items-center text-red-600"><XCircle className="w-5 h-5 mr-1"/> Discrepancy</span>
                  }
                </TableCell>
              </TableRow>
            ))}
            {results.length === 0 && !loading && (
              <TableRow>
                <TableCell colSpan={4} className="text-center text-slate-500 py-8">
                  Run reconciliation to view results.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
};"""

}

for path, content in files.items():
    full_path = os.path.join(base_dir, path)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, 'w', encoding='utf-8') as f:
        f.write(content)

print("Features created.")
