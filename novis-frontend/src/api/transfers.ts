import axiosInstance from './axiosInstance';

export const createTransfer = (data: any, idempotencyKey: string) =>
  axiosInstance.post('/transfers', data, { headers: { 'Idempotency-Key': idempotencyKey } });
export const getTransferById = (id: number) => axiosInstance.get(`/transfers/${id}`);