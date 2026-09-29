import React, { useState, useEffect } from 'react';
import { getMyAccounts } from '../../api/accounts';
import { createTransfer } from '../../api/transfers';
import { Account } from '../../types';

const CURRENCIES = [
  { code: 'USD', name: 'US Dollar',          flag: '🇺🇸', rate: 1 },
  { code: 'EUR', name: 'Euro',               flag: '🇪🇺', rate: 0.92 },
  { code: 'GBP', name: 'British Pound',      flag: '🇬🇧', rate: 0.79 },
  { code: 'INR', name: 'Indian Rupee',       flag: '🇮🇳', rate: 83.5 },
  { code: 'JPY', name: 'Japanese Yen',       flag: '🇯🇵', rate: 149.5 },
  { code: 'AED', name: 'UAE Dirham',         flag: '🇦🇪', rate: 3.67 },
  { code: 'SGD', name: 'Singapore Dollar',   flag: '🇸🇬', rate: 1.34 },
  { code: 'CAD', name: 'Canadian Dollar',    flag: '🇨🇦', rate: 1.36 },
  { code: 'AUD', name: 'Australian Dollar',  flag: '🇦🇺', rate: 1.53 },
];

export const TransferPage = () => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [formData, setFormData] = useState({
    fromAccountId: '',
    toAccountNumber: '',
    amount: '',
    description: '',
    currency: 'USD',
  });
  const [idempotencyKey, setIdempotencyKey] = useState(crypto.randomUUID());
  const [status, setStatus] = useState<{ type: 'success' | 'error' | 'warning'; message: string } | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    getMyAccounts()
      .then(res => {
        const data = res?.data?.data;
        const arr: Account[] = Array.isArray(data) ? data : [];
        setAccounts(arr);
        if (arr.length > 0) setFormData(prev => ({ ...prev, fromAccountId: arr[0].id.toString() }));
      })
      .catch(err => console.error('Failed to load accounts:', err));
  }, []);

  const selectedCurrency = CURRENCIES.find(c => c.code === formData.currency) ?? CURRENCIES[0];
  const amountNum = parseFloat(formData.amount) || 0;
  const amountInUSD = formData.currency === 'USD' ? amountNum : amountNum / selectedCurrency.rate;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setStatus(null);
    try {
      const payload = {
        fromAccountId: Number(formData.fromAccountId),
        toAccountNumber: formData.toAccountNumber,
        amount: amountInUSD, // always send in USD to backend
        currency: 'USD',
        description: formData.description,
      };
      const res = await createTransfer(payload, idempotencyKey);
      const tx = res.data.data;
      if (tx.status === 'FLAGGED') {
        setStatus({ type: 'warning', message: `⚠️ Transfer is under fraud review (ID: ${tx.id})` });
      } else {
        setStatus({ type: 'success', message: `✅ Transfer completed successfully (ID: ${tx.id})` });
      }
      setFormData(prev => ({ ...prev, toAccountNumber: '', amount: '', description: '' }));
      setIdempotencyKey(crypto.randomUUID());
    } catch (err: any) {
      setStatus({ type: 'error', message: err.response?.data?.message || 'Transfer failed. Check KYC status.' });
    } finally {
      setLoading(false);
    }
  };

  const statusColors = {
    success: 'bg-green-50 border-green-200 text-green-800',
    warning: 'bg-amber-50 border-amber-200 text-amber-800',
    error: 'bg-red-50 border-red-200 text-red-800',
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <h1 className="text-2xl font-bold text-slate-900">Transfer Funds</h1>

      <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
        {status && (
          <div className={`mb-6 p-4 rounded-lg border text-sm font-medium ${statusColors[status.type]}`}>
            {status.message}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          {/* From Account */}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">From Account</label>
            <select className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              value={formData.fromAccountId}
              onChange={e => setFormData({ ...formData, fromAccountId: e.target.value })} required>
              {accounts.length === 0
                ? <option disabled value="">No accounts available</option>
                : accounts.map(acc => (
                  <option key={acc.id} value={acc.id}>
                    {acc.accountType} ••••{acc.accountNumber?.slice(-4)} — ${(acc.balance ?? 0).toFixed(2)} USD
                  </option>
                ))
              }
            </select>
          </div>

          {/* To Account */}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Recipient Account Number</label>
            <input type="text" required
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              placeholder="Enter full account number"
              value={formData.toAccountNumber}
              onChange={e => setFormData({ ...formData, toAccountNumber: e.target.value })} />
          </div>

          {/* Amount + Currency */}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Amount & Currency</label>
            <div className="flex gap-2">
              <div className="relative flex-1">
                <span className="absolute left-3 top-1/2 -translate-y-1/2 text-lg">{selectedCurrency.flag}</span>
                <input type="number" step="0.01" min="0.01" required
                  className="w-full rounded-md border border-slate-300 pl-9 pr-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  placeholder="0.00"
                  value={formData.amount}
                  onChange={e => setFormData({ ...formData, amount: e.target.value })} />
              </div>
              <select className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 w-32"
                value={formData.currency}
                onChange={e => setFormData({ ...formData, currency: e.target.value })}>
                {CURRENCIES.map(c => (
                  <option key={c.code} value={c.code}>{c.flag} {c.code}</option>
                ))}
              </select>
            </div>
          </div>

          {/* Exchange Rate Preview */}
          {amountNum > 0 && (
            <div className="bg-gradient-to-r from-indigo-50 to-purple-50 border border-indigo-100 rounded-xl p-4">
              <p className="text-xs text-indigo-500 font-semibold uppercase tracking-wider mb-3">💱 Exchange Rate Preview</p>
              <div className="flex items-center justify-between gap-4">
                <div className="text-center flex-1">
                  <p className="text-xs text-slate-500 mb-1">{selectedCurrency.flag} You send</p>
                  <p className="text-2xl font-bold text-slate-900">{amountNum.toLocaleString('en', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p>
                  <p className="text-xs font-medium text-slate-600">{selectedCurrency.code}</p>
                </div>
                <div className="text-2xl text-slate-300">→</div>
                <div className="text-center flex-1">
                  <p className="text-xs text-slate-500 mb-1">🇺🇸 USD equivalent</p>
                  <p className="text-2xl font-bold text-indigo-700">{amountInUSD.toLocaleString('en', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p>
                  <p className="text-xs font-medium text-slate-600">USD</p>
                </div>
              </div>
              {formData.currency !== 'USD' && (
                <div className="mt-3 pt-3 border-t border-indigo-100 flex justify-between text-xs text-slate-500">
                  <span>Rate: 1 USD = {selectedCurrency.rate} {selectedCurrency.code}</span>
                  <span className="text-indigo-500">*Indicative rate</span>
                </div>
              )}
            </div>
          )}

          {/* Description */}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Description</label>
            <textarea className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
              rows={2} required
              placeholder="e.g. Rent payment, Invoice #123..."
              value={formData.description}
              onChange={e => setFormData({ ...formData, description: e.target.value })} />
          </div>

          {/* Idempotency Key */}
          <div className="bg-slate-50 rounded-lg p-3 border border-slate-100">
            <p className="text-xs text-slate-500 font-medium mb-1">🔑 Idempotency Key (prevents duplicate transfers)</p>
            <div className="flex gap-2 items-center">
              <code className="text-xs text-slate-600 flex-1 truncate">{idempotencyKey}</code>
              <button type="button" onClick={() => setIdempotencyKey(crypto.randomUUID())}
                className="text-xs text-indigo-600 hover:text-indigo-800 font-medium whitespace-nowrap border border-indigo-200 rounded px-2 py-0.5">
                ↻ New
              </button>
            </div>
          </div>

          <button type="submit" disabled={loading || accounts.length === 0}
            className="w-full bg-indigo-600 text-white py-3 rounded-md text-sm font-semibold hover:bg-indigo-700 disabled:opacity-50 transition-colors">
            {loading ? 'Processing...' : `Send ${formData.amount || '0'} ${formData.currency}`}
          </button>
        </form>
      </div>
    </div>
  );
};