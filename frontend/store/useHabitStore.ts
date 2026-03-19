import { create } from 'zustand';
import { Habit } from '../api/habitApi';

interface HabitState {
  habits: Habit[];
  setHabits: (habits: Habit[]) => void;
  toggleComplete: (habitId: number) => void;
  addHabit: (habit: Habit) => void;
  updateHabit: (habit: Habit) => void;
  removeHabit: (habitId: number) => void;
}

export const useHabitStore = create<HabitState>((set) => ({
  habits: [],

  setHabits: (habits) => set({ habits }),

  toggleComplete: (habitId) =>
    set((state) => ({
      habits: state.habits.map((h) =>
        h.id === habitId
          ? {
              ...h,
              completedToday: !h.completedToday,
              currentStreak: h.completedToday
                ? (h.currentStreak ?? 1) - 1
                : (h.currentStreak ?? 0) + 1,
            }
          : h
      ),
    })),

  addHabit: (habit) =>
    set((state) => ({ habits: [...state.habits, habit] })),

  updateHabit: (habit) =>
    set((state) => ({
      habits: state.habits.map((h) => (h.id === habit.id ? habit : h)),
    })),

  removeHabit: (habitId) =>
    set((state) => ({
      habits: state.habits.filter((h) => h.id !== habitId),
    })),
}));
