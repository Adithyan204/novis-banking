import os

base_dir = "/Users/adithyanmenont/Anti gravity/Bank_service/novis-frontend"

files = {
    "src/components/Button.tsx": """import React from 'react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
}

export const Button: React.FC<ButtonProps> = ({ variant = 'primary', size = 'md', className, ...props }) => {
  const base = "inline-flex items-center justify-center rounded-md font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 disabled:pointer-events-none disabled:opacity-50";
  const variants = {
    primary: "bg-indigo-600 text-white hover:bg-indigo-700 shadow-sm",
    secondary: "bg-slate-100 text-slate-900 hover:bg-slate-200",
    danger: "bg-red-600 text-white hover:bg-red-700 shadow-sm",
    ghost: "hover:bg-slate-100 hover:text-slate-900",
  };
  const sizes = {
    sm: "h-8 px-3 text-xs",
    md: "h-10 px-4 py-2 text-sm",
    lg: "h-12 px-8 text-base",
  };
  return (
    <button className={cn(base, variants[variant], sizes[size], className)} {...props} />
  );
};""",

    "src/components/Input.tsx": """import React from 'react';
import { cn } from './Button';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
}

export const Input: React.FC<InputProps> = ({ label, error, className, ...props }) => {
  return (
    <div className="w-full">
      {label && <label className="block text-sm font-medium text-slate-700 mb-1">{label}</label>}
      <input
        className={cn(
          "flex h-10 w-full rounded-md border border-slate-300 bg-transparent px-3 py-2 text-sm placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 disabled:cursor-not-allowed disabled:opacity-50",
          error && "border-red-500 focus:ring-red-500",
          className
        )}
        {...props}
      />
      {error && <p className="mt-1 text-sm text-red-500">{error}</p>}
    </div>
  );
};""",

    "src/components/Badge.tsx": """import React from 'react';
import { cn } from './Button';

interface BadgeProps {
  children: React.ReactNode;
  variant?: 'success' | 'warning' | 'danger' | 'info' | 'default';
  className?: string;
}

export const Badge: React.FC<BadgeProps> = ({ children, variant = 'default', className }) => {
  const variants = {
    success: "bg-green-100 text-green-800",
    warning: "bg-amber-100 text-amber-800",
    danger: "bg-red-100 text-red-800",
    info: "bg-blue-100 text-blue-800",
    default: "bg-slate-100 text-slate-800",
  };
  return (
    <span className={cn("inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold", variants[variant], className)}>
      {children}
    </span>
  );
};""",

    "src/components/Table.tsx": """import React from 'react';
import { cn } from './Button';

export const Table = ({ className, ...props }: React.HTMLAttributes<HTMLTableElement>) => (
  <div className="w-full overflow-auto rounded-lg border border-slate-200">
    <table className={cn("w-full caption-bottom text-sm", className)} {...props} />
  </div>
);
export const TableHeader = ({ className, ...props }: React.HTMLAttributes<HTMLTableSectionElement>) => (
  <thead className={cn("bg-slate-50", className)} {...props} />
);
export const TableBody = ({ className, ...props }: React.HTMLAttributes<HTMLTableSectionElement>) => (
  <tbody className={cn("[&_tr:last-child]:border-0", className)} {...props} />
);
export const TableRow = ({ className, ...props }: React.HTMLAttributes<HTMLTableRowElement>) => (
  <tr className={cn("border-b border-slate-200 transition-colors hover:bg-slate-50 data-[state=selected]:bg-slate-100", className)} {...props} />
);
export const TableHead = ({ className, ...props }: React.ThHTMLAttributes<HTMLTableCellElement>) => (
  <th className={cn("h-10 px-4 text-left align-middle font-medium text-slate-500", className)} {...props} />
);
export const TableCell = ({ className, ...props }: React.TdHTMLAttributes<HTMLTableCellElement>) => (
  <td className={cn("p-4 align-middle", className)} {...props} />
);""",

    "src/components/Modal.tsx": """import React from 'react';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
}

export const Modal: React.FC<ModalProps> = ({ isOpen, onClose, title, children }) => {
  if (!isOpen) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="bg-white rounded-lg shadow-lg w-full max-w-md p-6 relative">
        <h2 className="text-xl font-bold mb-4">{title}</h2>
        {children}
        <button onClick={onClose} className="absolute top-4 right-4 text-slate-400 hover:text-slate-600">✕</button>
      </div>
    </div>
  );
};""",

    "src/components/Spinner.tsx": """import React from 'react';

export const Spinner: React.FC = () => (
  <div className="flex justify-center items-center">
    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
  </div>
);""",

    "src/components/Alert.tsx": """import React from 'react';
import { cn } from './Button';

interface AlertProps {
  variant?: 'success' | 'danger' | 'warning' | 'info';
  children: React.ReactNode;
  className?: string;
}

export const Alert: React.FC<AlertProps> = ({ variant = 'info', children, className }) => {
  const variants = {
    success: 'bg-green-50 text-green-900 border-green-200',
    danger: 'bg-red-50 text-red-900 border-red-200',
    warning: 'bg-amber-50 text-amber-900 border-amber-200',
    info: 'bg-blue-50 text-blue-900 border-blue-200',
  };
  return (
    <div className={cn('p-4 rounded-md border text-sm', variants[variant], className)}>
      {children}
    </div>
  );
};""",

    "src/components/Sidebar.tsx": """import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Shield, LayoutDashboard, Send, FileCheck, Users, AlertTriangle, FileSpreadsheet, LogOut, FileText } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';

export const Sidebar: React.FC = () => {
  const { user, isAdmin, logout } = useAuth();
  const location = useLocation();

  const navItems = [
    { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { name: 'Transfer', path: '/transfer', icon: Send },
    { name: 'KYC Status', path: '/kyc', icon: FileCheck },
  ];

  const adminItems = [
    { name: 'KYC Queue', path: '/admin/kyc', icon: Users },
    { name: 'Fraud Flags', path: '/admin/fraud', icon: AlertTriangle },
    { name: 'Reconciliation', path: '/admin/reconciliation', icon: FileSpreadsheet },
  ];

  return (
    <div className="w-64 bg-slate-900 text-slate-300 flex flex-col h-screen fixed">
      <div className="h-16 flex items-center px-6 border-b border-slate-800">
        <Shield className="w-6 h-6 text-indigo-500 mr-2" />
        <span className="text-white text-xl font-bold">Novis</span>
      </div>
      
      <div className="flex-1 overflow-y-auto py-4">
        <div className="px-3 mb-2 text-xs font-semibold uppercase tracking-wider text-slate-500">Menu</div>
        {navItems.map((item) => (
          <Link
            key={item.name}
            to={item.path}
            className={`flex items-center px-6 py-3 text-sm hover:bg-slate-800 hover:text-white transition-colors ${location.pathname === item.path ? 'bg-indigo-600/10 text-indigo-400 border-r-2 border-indigo-500' : ''}`}
          >
            <item.icon className="w-5 h-5 mr-3" />
            {item.name}
          </Link>
        ))}

        {isAdmin && (
          <>
            <div className="px-3 mt-8 mb-2 text-xs font-semibold uppercase tracking-wider text-slate-500">Admin</div>
            {adminItems.map((item) => (
              <Link
                key={item.name}
                to={item.path}
                className={`flex items-center px-6 py-3 text-sm hover:bg-slate-800 hover:text-white transition-colors ${location.pathname === item.path ? 'bg-indigo-600/10 text-indigo-400 border-r-2 border-indigo-500' : ''}`}
              >
                <item.icon className="w-5 h-5 mr-3" />
                {item.name}
              </Link>
            ))}
          </>
        )}
      </div>

      <div className="p-4 border-t border-slate-800">
        <div className="flex items-center mb-4">
          <div className="w-8 h-8 rounded-full bg-slate-700 flex items-center justify-center text-white font-medium mr-3">
            {user?.fullName?.charAt(0) || 'U'}
          </div>
          <div>
            <div className="text-sm font-medium text-white">{user?.fullName}</div>
            <div className="text-xs text-slate-500">{user?.role}</div>
          </div>
        </div>
        <button
          onClick={logout}
          className="flex items-center w-full px-2 py-2 text-sm text-slate-400 hover:text-white hover:bg-slate-800 rounded-md transition-colors"
        >
          <LogOut className="w-4 h-4 mr-2" />
          Sign Out
        </button>
      </div>
    </div>
  );
};""",

    "src/components/Layout.tsx": """import React from 'react';
import { Sidebar } from './Sidebar';

export const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <div className="flex min-h-screen bg-slate-50">
      <Sidebar />
      <main className="flex-1 ml-64 p-8 overflow-y-auto">
        {children}
      </main>
    </div>
  );
};""",

    "src/routes/PrivateRoute.tsx": """import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { Layout } from '../components/Layout';
import { Spinner } from '../components/Spinner';

export const PrivateRoute: React.FC = () => {
  const { isAuthenticated, isLoading } = useAuth();
  
  if (isLoading) return <div className="h-screen flex items-center justify-center"><Spinner /></div>;
  if (!isAuthenticated) return <Navigate to="/login" replace />;

  return <Layout><Outlet /></Layout>;
};""",

    "src/routes/AdminRoute.tsx": """import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { Layout } from '../components/Layout';
import { Spinner } from '../components/Spinner';

export const AdminRoute: React.FC = () => {
  const { isAuthenticated, isAdmin, isLoading } = useAuth();
  
  if (isLoading) return <div className="h-screen flex items-center justify-center"><Spinner /></div>;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (!isAdmin) return <Navigate to="/dashboard" replace />;

  return <Layout><Outlet /></Layout>;
};""",

    "src/routes/AppRouter.tsx": """import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { PrivateRoute } from './PrivateRoute';
import { AdminRoute } from './AdminRoute';
import { LoginPage } from '../features/auth/LoginPage';
import { RegisterPage } from '../features/auth/RegisterPage';
import { MfaPage } from '../features/auth/MfaPage';
import { DashboardPage } from '../features/dashboard/DashboardPage';
import { TransferPage } from '../features/transfer/TransferPage';
import { KycPage } from '../features/kyc/KycPage';
import { KycQueuePage } from '../features/admin/KycQueuePage';
import { FraudFlagsPage } from '../features/admin/FraudFlagsPage';
import { ReconciliationPage } from '../features/admin/ReconciliationPage';

export const AppRouter = () => {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/mfa" element={<MfaPage />} />
      
      <Route element={<PrivateRoute />}>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/transfer" element={<TransferPage />} />
        <Route path="/kyc" element={<KycPage />} />
      </Route>

      <Route element={<AdminRoute />}>
        <Route path="/admin/kyc" element={<KycQueuePage />} />
        <Route path="/admin/fraud" element={<FraudFlagsPage />} />
        <Route path="/admin/reconciliation" element={<ReconciliationPage />} />
      </Route>
    </Routes>
  );
};"""

}

for path, content in files.items():
    full_path = os.path.join(base_dir, path)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, 'w', encoding='utf-8') as f:
        f.write(content)

print("Components and routes created.")
