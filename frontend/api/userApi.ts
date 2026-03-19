import api from './client';

export const userApi = {
  deleteAccount: () => api.delete('/users/me'),
};
