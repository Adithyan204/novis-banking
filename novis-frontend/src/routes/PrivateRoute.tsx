import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { Layout } from '../components/Layout';
import { Spinner } from '../components/Spinner';

export const PrivateRoute: React.FC = () => {
  const { isAuthenticated, isLoading } = useAuth();
  
  if (isLoading) return <div className="h-screen flex items-center justify-center"><Spinner /></div>;
  if (!isAuthenticated) return <Navigate to="/login" replace />;

  return <Layout><Outlet /></Layout>;
};