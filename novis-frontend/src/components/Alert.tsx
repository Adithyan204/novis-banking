import React from 'react';
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
};