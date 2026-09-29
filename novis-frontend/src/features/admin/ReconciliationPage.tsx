import React, { useState } from 'react';
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
};