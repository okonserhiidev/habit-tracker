import api from './client';

export interface HabitSummary {
  id: number;
  name: string;
  color: string;
  completionRate: number;
  currentStreak: number;
}

export interface DashboardResponse {
  activeHabits: number;
  overallCompletionRate: number;
  perfectDays: number;
  perfectDayStreak: number;
  topHabits: HabitSummary[];
  needAttention: HabitSummary[];
  completionByDayOfWeek: Record<string, number>;
}

export interface DailyPoint {
  date: string;
  completed: number;
  total: number;
  rate: number;
}

export interface TrendsResponse {
  currentWeekRate: number;
  previousWeekRate: number;
  changePercent: number;
  currentMonthRate: number;
  previousMonthRate: number;
  monthChangePercent: number;
  dailyTrend: DailyPoint[];
}

export interface AchievementResponse {
  id: number;
  type: string;
  milestoneDays: number | null;
  habitId: number | null;
  unlockedAt: string;
}

export interface ChainHabitItem {
  habitId: number;
  habitName: string;
  habitColor: string;
  position: number;
}

export interface ChainResponse {
  id: number;
  name: string;
  type: string;
  habits: ChainHabitItem[];
}

export const analyticsApi = {
  getDashboard: () =>
    api.get<DashboardResponse>('/analytics/dashboard').then((r) => r.data),
  getTrends: () =>
    api.get<TrendsResponse>('/analytics/trends').then((r) => r.data),
  getAchievements: () =>
    api.get<AchievementResponse[]>('/achievements').then((r) => r.data),
};

export const freezeApi = {
  getFreezeDays: () =>
    api
      .get<{ freezeDays: any[]; remainingThisWeek: number }>('/freeze-days')
      .then((r) => r.data),
  freeze: (habitId: number, date: string) =>
    api.post(`/habits/${habitId}/freeze`, { date }).then((r) => r.data),
};

export const chainApi = {
  getAll: () => api.get<ChainResponse[]>('/chains').then((r) => r.data),
  create: (data: { name: string; type: string; habitIds: number[] }) =>
    api.post<ChainResponse>('/chains', data).then((r) => r.data),
  update: (
    id: number,
    data: { name: string; type: string; habitIds: number[] }
  ) => api.put<ChainResponse>(`/chains/${id}`, data).then((r) => r.data),
  delete: (id: number) => api.delete(`/chains/${id}`),
};
