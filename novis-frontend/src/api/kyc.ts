import axiosInstance from './axiosInstance';

export const uploadDocument = (documentType: string, file: File) => {
  const form = new FormData();
  form.append('documentType', documentType);
  form.append('file', file);
  return axiosInstance.post('/kyc/documents', form, { headers: { 'Content-Type': 'multipart/form-data' } });
};
export const getKycStatus = () => axiosInstance.get('/kyc/status');