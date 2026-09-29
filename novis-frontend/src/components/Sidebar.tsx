import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Shield, LayoutDashboard, Send, FileCheck, Users, AlertTriangle, FileSpreadsheet, LogOut, History, Bell } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';

export const Sidebar: React.FC = () => {
  const { user, isAdmin, logout } = useAuth();
  const location = useLocation();

  const navItems = [
    { name: 'Dashboard',      path: '/dashboard',     icon: LayoutDashboard },
    { name: 'Transfer',       path: '/transfer',      icon: Send },
    { name: 'Transactions',   path: '/transactions',  icon: History },
    { name: 'KYC Status',     path: '/kyc',           icon: FileCheck },
    { name: 'Notifications',  path: '/notifications', icon: Bell },
  ];

  const adminItems = [
    { name: 'KYC Queue',       path: '/admin/kyc',             icon: Users },
    { name: 'Fraud Flags',     path: '/admin/fraud',           icon: AlertTriangle },
    { name: 'Reconciliation',  path: '/admin/reconciliation',  icon: FileSpreadsheet },
  ];

  const isActive = (path: string) => location.pathname === path;

  const initials = (user?.fullName ?? 'U')
    .split(' ')
    .map((w: string) => w[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);

  return (
    <div className="w-64 bg-slate-900 text-slate-300 flex flex-col h-screen fixed z-40">
      {/* Logo */}
      <div className="h-16 flex items-center px-6 border-b border-slate-800 flex-shrink-0">
        <Shield className="w-6 h-6 text-indigo-500 mr-2" />
        <span className="text-white text-xl font-bold">Novis</span>
      </div>

      {/* Nav */}
      <div className="flex-1 overflow-y-auto py-4">
        <div className="px-3 mb-2 text-xs font-semibold uppercase tracking-wider text-slate-500">Menu</div>
        {navItems.map(item => (
          <Link key={item.name} to={item.path}
            className={`flex items-center px-6 py-3 text-sm hover:bg-slate-800 hover:text-white transition-colors ${
              isActive(item.path) ? 'bg-indigo-600/10 text-indigo-400 border-r-2 border-indigo-500' : ''
            }`}>
            <item.icon className="w-5 h-5 mr-3" />
            {item.name}
          </Link>
        ))}

        {isAdmin && (
          <>
            <div className="px-3 mt-8 mb-2 text-xs font-semibold uppercase tracking-wider text-slate-500">Admin</div>
            {adminItems.map(item => (
              <Link key={item.name} to={item.path}
                className={`flex items-center px-6 py-3 text-sm hover:bg-slate-800 hover:text-white transition-colors ${
                  isActive(item.path) ? 'bg-indigo-600/10 text-indigo-400 border-r-2 border-indigo-500' : ''
                }`}>
                <item.icon className="w-5 h-5 mr-3" />
                {item.name}
              </Link>
            ))}
          </>
        )}
      </div>

      {/* Profile + Sign Out */}
      <div className="p-4 border-t border-slate-800 flex-shrink-0">
        <Link to="/profile"
          className="flex items-center p-2 rounded-lg hover:bg-slate-800 transition-colors cursor-pointer mb-2 group">
          <div className="w-9 h-9 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white font-bold text-sm mr-3 flex-shrink-0">
            {initials}
          </div>
          <div className="flex-1 min-w-0">
            <div className="text-sm font-medium text-white truncate">{user?.fullName ?? 'User'}</div>
            <div className="text-xs text-slate-500">{user?.role ?? 'CUSTOMER'}</div>
          </div>
          <svg className="w-4 h-4 text-slate-600 group-hover:text-slate-400 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
          </svg>
        </Link>
        <button onClick={logout}
          className="flex items-center w-full px-2 py-2 text-sm text-slate-400 hover:text-white hover:bg-slate-800 rounded-md transition-colors">
          <LogOut className="w-4 h-4 mr-2" />
          Sign Out
        </button>
      </div>
    </div>
  );
};