import React, { useEffect, useState } from 'react';
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
};