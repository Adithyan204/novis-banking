import axiosInstance from './axiosInstance';

export const getPendingKyc = () => axiosInstance.get('/admin/kyc/pending');
export const reviewKyc = (id: number, data: { action: string; rejectionReason?: string }) => axiosInstance.post(`/admin/kyc/${id}/review`, data);
export const getFraudFlags = () => axiosInstance.get('/admin/fraud/flags');
export const resolveFlag = (id: number, data: { action: string; reviewNote?: string }) => axiosInstance.post(`/admin/fraud/flags/${id}/resolve`, data);
export const getReconciliation = () => axiosInstance.get('/admin/reconciliation');
export const getAuditLogs = (page = 0) => axiosInstance.get(`/admin/audit-logs?page=${page}&size=50`);