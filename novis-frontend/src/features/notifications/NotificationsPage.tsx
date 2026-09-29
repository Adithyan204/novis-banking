import React, { useEffect, useState } from 'react';
import { getMyActivity } from '../../api/users';

const ACTION_ICONS: Record<string, string> = {
  REGISTER: '🎉',
  LOGIN: '🔐',
  LOGOUT: '🚪',
  LOGIN_ATTEMPT: '🔑',
  LOGIN_SUCCESS: '✅',
  MFA_VERIFIED: '🛡️',
  TRANSFER_INITIATED: '💸',
  TRANSFER_COMPLETED: '✅',
  TRANSFER_FLAGGED: '🚨',
  KYC_UPLOADED: '📄',
  KYC_APPROVED: '✅',
  KYC_REJECTED: '❌',
  FRAUD_FLAG_RESOLVED: '🔍',
  RECONCILIATION_RUN: '📊',
  PASSWORD_CHANGED: '🔑',
};

const ACTION_COLORS: Record<string, string> = {
  TRANSFER_FLAGGED: 'border-l-red-400 bg-red-50',
  KYC_REJECTED: 'border-l-red-400 bg-red-50',
  TRANSFER_COMPLETED: 'border-l-green-400 bg-green-50',
  KYC_APPROVED: 'border-l-green-400 bg-green-50',
  REGISTER: 'border-l-indigo-400 bg-indigo-50',
};

export const NotificationsPage = () => {
  const [activities, setActivities] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getMyActivity()
      .then(res => {
        const data = res?.data?.data;
        setActivities(Array.isArray(data) ? data : []);
      })
      .catch(err => {
        setError(err.response?.data?.message || 'Could not load activity. Make sure you are logged in.');
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-900">Activity Feed</h1>
        <span className="text-sm text-slate-500">{activities.length} recent events</span>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm">
        {loading ? (
          <div className="flex items-center justify-center h-32">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600" />
          </div>
        ) : error ? (
          <div className="p-6 text-red-600 text-sm">❌ {error}</div>
        ) : activities.length === 0 ? (
          <div className="text-center py-16 text-slate-400">
            <p className="text-4xl mb-3">🔔</p>
            <p>No activity yet</p>
          </div>
        ) : (
          <ul className="divide-y divide-slate-100">
            {activities.map((act, i) => (
              <li key={act.id ?? i}
                className={`flex items-start gap-4 p-4 border-l-4 border-l-transparent hover:bg-slate-50 transition-colors ${ACTION_COLORS[act.action] || ''}`}>
                <span className="text-xl mt-0.5 flex-shrink-0">{ACTION_ICONS[act.action] || '📌'}</span>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-semibold text-slate-900">{act.action?.replace(/_/g, ' ')}</p>
                  {act.details && (
                    <p className="text-xs text-slate-500 mt-0.5 truncate">{act.details}</p>
                  )}
                  {act.ipAddress && (
                    <p className="text-xs text-slate-400 mt-0.5">From IP: {act.ipAddress}</p>
                  )}
                </div>
                <time className="text-xs text-slate-400 whitespace-nowrap mt-0.5 flex-shrink-0">
                  {act.createdAt ? new Date(act.createdAt).toLocaleString() : ''}
                </time>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};
