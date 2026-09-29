import React, { useEffect, useState } from 'react';
import { getMyAccounts, getAccountTransactions } from '../../api/accounts';

const STATUS_COLORS: Record<string, string> = {
  COMPLETED: 'bg-green-100 text-green-800',
  PENDING: 'bg-amber-100 text-amber-800',
  FLAGGED: 'bg-red-100 text-red-800',
  FAILED: 'bg-slate-100 text-slate-600',
};

export const TransactionsPage = () => {
  const [accounts, setAccounts] = useState<any[]>([]);
  const [selectedAccountId, setSelectedAccountId] = useState<number | null>(null);
  const [transactions, setTransactions] = useState<any[]>([]);
  const [filtered, setFiltered] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [search, setSearch] = useState('');

  useEffect(() => {
    getMyAccounts().then(res => {
      const accs = res?.data?.data;
      const arr = Array.isArray(accs) ? accs : [];
      setAccounts(arr);
      if (arr.length > 0) setSelectedAccountId(arr[0].id);
    }).catch(console.error);
  }, []);

  useEffect(() => {
    if (!selectedAccountId) return;
    setLoading(true);
    getAccountTransactions(selectedAccountId)
      .then(res => {
        const data = res?.data?.data;
        const txs = data?.content ?? (Array.isArray(data) ? data : []);
        setTransactions(txs);
        setFiltered(txs);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [selectedAccountId]);

  useEffect(() => {
    let result = transactions;
    if (statusFilter !== 'ALL') result = result.filter(tx => tx.status === statusFilter);
    if (search) result = result.filter(tx =>
      tx.description?.toLowerCase().includes(search.toLowerCase()) ||
      String(tx.amount).includes(search)
    );
    setFiltered(result);
  }, [statusFilter, search, transactions]);

  const exportCSV = () => {
    const header = 'Date,Description,Amount,Currency,Status,Type';
    const rows = filtered.map(tx => {
      const isCredit = selectedAccountId && tx.toAccountId === selectedAccountId;
      return [
        new Date(tx.createdAt).toLocaleDateString(),
        `"${(tx.description ?? '').replace(/"/g, "'")}"`,
        tx.amount,
        tx.currency,
        tx.status,
        isCredit ? 'CREDIT' : 'DEBIT',
      ].join(',');
    });
    const csv = [header, ...rows].join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = 'novis-transactions.csv'; a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center flex-wrap gap-3">
        <h1 className="text-2xl font-bold text-slate-900">Transaction History</h1>
        <button onClick={exportCSV}
          className="bg-white border border-slate-200 text-slate-700 px-4 py-2 rounded-lg text-sm font-medium hover:bg-slate-50 flex items-center gap-2 shadow-sm">
          ⬇ Export CSV
        </button>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-4 flex flex-wrap gap-3 items-center">
        {accounts.length > 1 && (
          <select className="rounded-md border border-slate-300 px-3 py-1.5 text-sm"
            value={selectedAccountId ?? ''}
            onChange={e => setSelectedAccountId(Number(e.target.value))}>
            {accounts.map(a => <option key={a.id} value={a.id}>{a.accountType} ••••{a.accountNumber?.slice(-4)}</option>)}
          </select>
        )}
        <select className="rounded-md border border-slate-300 px-3 py-1.5 text-sm"
          value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
          <option value="ALL">All Status</option>
          <option value="COMPLETED">Completed</option>
          <option value="PENDING">Pending</option>
          <option value="FLAGGED">Flagged</option>
          <option value="FAILED">Failed</option>
        </select>
        <input type="text" placeholder="Search description or amount..."
          className="flex-1 min-w-40 rounded-md border border-slate-300 px-3 py-1.5 text-sm"
          value={search} onChange={e => setSearch(e.target.value)} />
        <span className="text-sm text-slate-500">{filtered.length} results</span>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="flex items-center justify-center h-32">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600" />
          </div>
        ) : filtered.length === 0 ? (
          <div className="text-center py-16 text-slate-400">
            <p className="text-4xl mb-3">📋</p>
            <p>No transactions found</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 border-b border-slate-200">
                <tr>
                  <th className="text-left px-4 py-3 font-medium text-slate-500">Date</th>
                  <th className="text-left px-4 py-3 font-medium text-slate-500">Description</th>
                  <th className="text-right px-4 py-3 font-medium text-slate-500">Amount</th>
                  <th className="text-left px-4 py-3 font-medium text-slate-500">Status</th>
                  <th className="text-left px-4 py-3 font-medium text-slate-500">Type</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filtered.map(tx => {
                  const isCredit = selectedAccountId && tx.toAccountId === selectedAccountId;
                  return (
                    <tr key={tx.id} className={`hover:bg-slate-50 transition-colors ${tx.status === 'FLAGGED' ? 'bg-red-50' : ''}`}>
                      <td className="px-4 py-3 text-slate-500 whitespace-nowrap">{new Date(tx.createdAt).toLocaleString()}</td>
                      <td className="px-4 py-3 font-medium text-slate-900 max-w-xs truncate">{tx.description || '—'}</td>
                      <td className={`px-4 py-3 text-right font-semibold whitespace-nowrap ${isCredit ? 'text-green-600' : 'text-slate-900'}`}>
                        {isCredit ? '+' : '-'}${tx.amount?.toFixed(2)} {tx.currency}
                      </td>
                      <td className="px-4 py-3">
                        <span className={`text-xs px-2.5 py-0.5 rounded-full font-semibold ${STATUS_COLORS[tx.status] || 'bg-slate-100 text-slate-600'}`}>{tx.status}</span>
                      </td>
                      <td className="px-4 py-3">
                        <span className={`text-xs font-medium ${isCredit ? 'text-green-600' : 'text-slate-500'}`}>{isCredit ? '↓ Credit' : '↑ Debit'}</span>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
