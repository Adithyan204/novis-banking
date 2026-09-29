import os

base_dir = "/Users/adithyanmenont/Anti gravity/Bank_service/novis-frontend"

files = {
    "package.json": """{
  "name": "novis-frontend",
  "version": "0.0.1",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "react": "^18.3.1",
    "react-dom": "^18.3.1",
    "react-router-dom": "^6.26.1",
    "axios": "^1.7.5",
    "recharts": "^2.12.7",
    "lucide-react": "^0.441.0",
    "clsx": "^2.0.0",
    "tailwind-merge": "^2.0.0"
  },
  "devDependencies": {
    "@types/react": "^18.3.5",
    "@types/react-dom": "^18.3.0",
    "@vitejs/plugin-react": "^4.3.1",
    "autoprefixer": "^10.4.20",
    "postcss": "^8.4.45",
    "tailwindcss": "^3.4.10",
    "typescript": "^5.5.3",
    "vite": "^5.4.2"
  }
}""",

    "vite.config.ts": """import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
})""",

    "tsconfig.json": """{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}""",

    "tsconfig.node.json": """{
  "compilerOptions": {
    "composite": true,
    "skipLibCheck": true,
    "module": "ESNext",
    "moduleResolution": "bundler",
    "allowSyntheticDefaultImports": true
  },
  "include": ["vite.config.ts"]
}""",

    "postcss.config.js": """export default {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
}""",

    "tailwind.config.js": """/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}""",

    "index.html": """<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Novis - Core Banking UI</title>
  </head>
  <body class="bg-slate-50 text-slate-900 font-sans antialiased">
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>""",

    "src/index.css": """@tailwind base;
@tailwind components;
@tailwind utilities;""",

    "src/main.tsx": """import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import './index.css'
import { AuthProvider } from './context/AuthContext'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <AuthProvider>
      <App />
    </AuthProvider>
  </React.StrictMode>,
)""",

    "src/App.tsx": """import { BrowserRouter } from 'react-router-dom';
import { AppRouter } from './routes/AppRouter';

function App() {
  return (
    <BrowserRouter>
      <AppRouter />
    </BrowserRouter>
  );
}

export default App;""",

    "src/types/index.ts": """export interface User {
  id: number;
  email: string;
  fullName: string;
  role: 'CUSTOMER' | 'ADMIN';
  kycStatus: string;
  mfaEnabled: boolean;
}

export interface Account {
  id: number;
  accountNumber: string;
  accountType: string;
  currency: string;
  status: string;
  balance: number;
  createdAt: string;
}

export interface Transaction {
  id: number;
  idempotencyKey: string;
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  currency: string;
  status: string;
  description: string;
  createdAt: string;
}

export interface FraudFlag {
  id: number;
  transactionId: number;
  ruleTriggered: string;
  severity: string;
  status: string;
  createdAt: string;
}

export interface KycDocument {
  id: number;
  documentType: string;
  status: string;
  rejectionReason?: string;
  createdAt: string;
}

export interface KycStatus {
  userId: number;
  kycStatus: string;
  documents: KycDocument[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  mfaRequired: boolean;
  mfaSessionToken?: string;
}
""",

    "src/api/axiosInstance.ts": """import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => Promise.reject(error));

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const res = await axios.post('/api/auth/refresh', { refreshToken });
          const { accessToken, refreshToken: newRefreshToken } = res.data.data;
          localStorage.setItem('accessToken', accessToken);
          localStorage.setItem('refreshToken', newRefreshToken);
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          return axiosInstance(originalRequest);
        } catch (err) {
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          window.location.href = '/login';
          return Promise.reject(err);
        }
      }
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;""",

    "src/api/auth.ts": """import axiosInstance from './axiosInstance';
import axios from 'axios';

export const register = (data: any) => axios.post('/api/auth/register', data);
export const login = (data: any) => axios.post('/api/auth/login', data);
export const verifyMfa = (data: any) => axios.post('/api/auth/mfa/verify', data);
export const refreshToken = (token: string) => axios.post('/api/auth/refresh', { refreshToken: token });
export const logout = () => axiosInstance.post('/auth/logout');""",

    "src/api/accounts.ts": """import axiosInstance from './axiosInstance';

export const getMyAccounts = () => axiosInstance.get('/accounts/me');
export const getAccountById = (id: number) => axiosInstance.get(`/accounts/${id}`);
export const getAccountTransactions = (id: number, page = 0) => axiosInstance.get(`/accounts/${id}/transactions?page=${page}&size=20`);""",

    "src/api/transfers.ts": """import axiosInstance from './axiosInstance';

export const createTransfer = (data: any, idempotencyKey: string) =>
  axiosInstance.post('/transfers', data, { headers: { 'Idempotency-Key': idempotencyKey } });
export const getTransferById = (id: number) => axiosInstance.get(`/transfers/${id}`);""",

    "src/api/kyc.ts": """import axiosInstance from './axiosInstance';

export const uploadDocument = (documentType: string, file: File) => {
  const form = new FormData();
  form.append('documentType', documentType);
  form.append('file', file);
  return axiosInstance.post('/kyc/documents', form, { headers: { 'Content-Type': 'multipart/form-data' } });
};
export const getKycStatus = () => axiosInstance.get('/kyc/status');""",

    "src/api/admin.ts": """import axiosInstance from './axiosInstance';

export const getPendingKyc = () => axiosInstance.get('/admin/kyc/pending');
export const reviewKyc = (id: number, data: { action: string; rejectionReason?: string }) => axiosInstance.post(`/admin/kyc/${id}/review`, data);
export const getFraudFlags = () => axiosInstance.get('/admin/fraud/flags');
export const resolveFlag = (id: number, data: { action: string; reviewNote?: string }) => axiosInstance.post(`/admin/fraud/flags/${id}/resolve`, data);
export const getReconciliation = () => axiosInstance.get('/admin/reconciliation');
export const getAuditLogs = (page = 0) => axiosInstance.get(`/admin/audit-logs?page=${page}&size=50`);""",

    "src/context/AuthContext.tsx": """import React, { createContext, useState, useEffect, ReactNode } from 'react';
import { User } from '../types';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isLoading: boolean;
  login: (token: string, refresh: string) => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType>({} as AuthContextType);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        setUser(payload.user || {
          id: payload.id,
          email: payload.sub,
          fullName: payload.fullName || 'User',
          role: payload.role || 'CUSTOMER',
          kycStatus: payload.kycStatus || 'NONE',
          mfaEnabled: payload.mfaEnabled || false,
        });
      } catch (e) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
      }
    }
    setIsLoading(false);
  }, []);

  const login = (token: string, refresh: string) => {
    localStorage.setItem('accessToken', token);
    localStorage.setItem('refreshToken', refresh);
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      setUser(payload.user || {
        id: payload.id,
        email: payload.sub,
        fullName: payload.fullName || 'User',
        role: payload.role || 'CUSTOMER',
        kycStatus: payload.kycStatus || 'NONE',
        mfaEnabled: payload.mfaEnabled || false,
      });
    } catch (e) {
      console.error(e);
    }
  };

  const logout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{
      user,
      isAuthenticated: !!user,
      isAdmin: user?.role === 'ADMIN',
      isLoading,
      login,
      logout
    }}>
      {children}
    </AuthContext.Provider>
  );
};""",

    "src/hooks/useAuth.ts": """import { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';

export const useAuth = () => {
  return useContext(AuthContext);
};""",

}

for path, content in files.items():
    full_path = os.path.join(base_dir, path)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, 'w', encoding='utf-8') as f:
        f.write(content)

print("Base files created.")
