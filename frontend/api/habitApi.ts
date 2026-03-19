import api from './client';

export interface Habit {
  id: number;
  name: string;
  description: string | null;
  color: string;
  icon: string | null;
  category: string | null;
  identityText: string | null;
  miniVersion: string | null;
  frequency: 'DAILY' | 'WEEKLY' | 'CUSTOM';
  customDays: string[] | null;
  reminderTime: string | null;
  position: number;
  isArchived: boolean;
  completedToday: boolean | null;
  currentStreak: number | null;
}

export interface CreateHabitRequest {
  name: string;
  description?: string;
  color: string;
  icon?: string;
  category?: string;
  identityText?: string;
  miniVersion?: string;
  frequency: 'DAILY' | 'WEEKLY' | 'CUSTOM';
  customDays?: string[];
  reminderTime?: string;
}

export interface HabitStats {
  currentStreak: number;
  bestStreak: number;
  totalCompletions: number;
  completionRateWeek: number;
  completionRateMonth: number;
  completionsByDayOfWeek: Record<string, number>;
}

export interface HeatmapEntry {
  date: string;
  completed: boolean;
}

export const habitApi = {
  getAll: () =>
    api.get<Habit[]>('/habits').then((r) => r.data),

  getById: (id: number) =>
    api.get<Habit>(`/habits/${id}`).then((r) => r.data),

  create: (data: CreateHabitRequest) =>
    api.post<Habit>('/habits', data).then((r) => r.data),

  update: (id: number, data: Partial<CreateHabitRequest>) =>
    api.put<Habit>(`/habits/${id}`, data).then((r) => r.data),

  delete: (id: number) =>
    api.delete(`/habits/${id}`),

  archive: (id: number) =>
    api.patch(`/habits/${id}/archive`),

  reorder: (habitIds: number[]) =>
    api.put('/habits/reorder', { habitIds }),

  complete: (id: number, date: string) =>
    api.post(`/habits/${id}/complete`, { date }).then((r) => r.data),

  uncomplete: (id: number, date: string) =>
    api.delete(`/habits/${id}/complete`, { data: { date } }),

  getCompletions: (id: number, start: string, end: string) =>
    api.get(`/habits/${id}/completions`, { params: { start, end } }).then((r) => r.data),

  getStats: (id: number) =>
    api.get<HabitStats>(`/habits/${id}/stats`).then((r) => r.data),

  getHeatmap: (id: number, year?: number) =>
    api.get<HeatmapEntry[]>(`/habits/${id}/heatmap`, { params: { year } }).then((r) => r.data),
};
