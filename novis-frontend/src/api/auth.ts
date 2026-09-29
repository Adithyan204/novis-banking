import axiosInstance from './axiosInstance';
import axios from 'axios';

export const register = (data: any) => axios.post('/api/auth/register', data);
export const login = (data: any) => axios.post('/api/auth/login', data);
export const verifyMfa = (data: any) => axios.post('/api/auth/mfa/verify', data);
export const refreshToken = (token: string) => axios.post('/api/auth/refresh', { refreshToken: token });
export const logout = () => axiosInstance.post('/auth/logout');