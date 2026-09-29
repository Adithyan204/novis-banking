import React from 'react';
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
};