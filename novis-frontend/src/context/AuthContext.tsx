import React, { createContext, useState, useEffect, ReactNode } from 'react';
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
          id: payload.userId,
          email: payload.sub,
          fullName: payload.fullName || payload.sub,
          role: payload.role || 'CUSTOMER',
          kycStatus: payload.kycStatus || 'PENDING',
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
        id: payload.userId,
        email: payload.sub,
        fullName: payload.fullName || payload.sub,
        role: payload.role || 'CUSTOMER',
        kycStatus: payload.kycStatus || 'PENDING',
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
};