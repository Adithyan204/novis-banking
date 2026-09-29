import axiosInstance from './axiosInstance';

export const getMyProfile = () => axiosInstance.get('/users/me');
export const updateMyProfile = (data: { fullName: string }) => axiosInstance.put('/users/me', data);
export const changePassword = (data: { currentPassword: string; newPassword: string }) =>
  axiosInstance.post('/users/me/change-password', data);
export const getMyActivity = () => axiosInstance.get('/users/me/activity');
