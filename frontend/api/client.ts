import axios from 'axios';
import { Platform } from 'react-native';
import { getItem, setItem, deleteItem } from '../utils/storage';

const DEV_IP = '192.168.210.198'; // your local network IP

const BASE_URL = __DEV__
  ? Platform.OS === 'web'
    ? 'http://localhost:8080/api'
    : `http://${DEV_IP}:8080/api`
  : 'https://api.habittracker.app/api';

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor — attach JWT
api.interceptors.request.use(async (config) => {
  const token = await getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor — refresh token on 401
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = await getItem('refreshToken');
        if (!refreshToken) {
          throw new Error('No refresh token');
        }

        const { data } = await axios.post(`${BASE_URL}/auth/refresh`, {
          refreshToken,
        });

        await setItem('accessToken', data.accessToken);
        await setItem('refreshToken', data.refreshToken);

        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        return api(originalRequest);
      } catch {
        await deleteItem('accessToken');
        await deleteItem('refreshToken');
      }
    }

    return Promise.reject(error);
  }
);

export default api;
