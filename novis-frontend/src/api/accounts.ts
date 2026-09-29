import axiosInstance from './axiosInstance';

export const getMyAccounts = () => axiosInstance.get('/accounts/me');
export const getAccountById = (id: number) => axiosInstance.get(`/accounts/${id}`);
export const getAccountTransactions = (id: number, page = 0) => axiosInstance.get(`/accounts/${id}/transactions?page=${page}&size=20`);