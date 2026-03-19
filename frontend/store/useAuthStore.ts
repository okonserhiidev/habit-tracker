import { create } from 'zustand';
import { getItem, setItem, deleteItem } from '../utils/storage';
import { authApi, AuthResponse } from '../api/authApi';

interface User {
  id: number;
  email: string;
  name: string;
  avatarUrl: string | null;
}

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, name: string) => Promise<void>;
  logout: () => Promise<void>;
  restoreSession: () => Promise<void>;
  clearError: () => void;
}

const saveTokens = async (data: AuthResponse) => {
  await setItem('accessToken', data.accessToken);
  await setItem('refreshToken', data.refreshToken);
  await setItem('user', JSON.stringify(data.user));
};

const clearTokens = async () => {
  await deleteItem('accessToken');
  await deleteItem('refreshToken');
  await deleteItem('user');
};

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  isLoading: true,
  error: null,

  login: async (email, password) => {
    set({ isLoading: true, error: null });
    try {
      const data = await authApi.login({ email, password });
      await saveTokens(data);
      set({ user: data.user, isAuthenticated: true, isLoading: false });
    } catch (err: any) {
      set({
        isLoading: false,
        error: err.response?.data?.error || 'Login failed',
      });
    }
  },

  register: async (email, password, name) => {
    set({ isLoading: true, error: null });
    try {
      const data = await authApi.register({ email, password, name });
      await saveTokens(data);
      set({ user: data.user, isAuthenticated: true, isLoading: false });
    } catch (err: any) {
      set({
        isLoading: false,
        error: err.response?.data?.error || 'Registration failed',
      });
    }
  },

  logout: async () => {
    await clearTokens();
    set({ user: null, isAuthenticated: false, isLoading: false, error: null });
  },

  restoreSession: async () => {
    try {
      const userJson = await getItem('user');
      const accessToken = await getItem('accessToken');
      if (userJson && accessToken) {
        set({ user: JSON.parse(userJson), isAuthenticated: true, isLoading: false });
      } else {
        set({ isLoading: false });
      }
    } catch {
      await clearTokens();
      set({ isLoading: false });
    }
  },

  clearError: () => set({ error: null }),
}));
