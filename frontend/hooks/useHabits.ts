import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { habitApi, CreateHabitRequest } from '../api/habitApi';
import { useHabitStore } from '../store/useHabitStore';
import { useEffect } from 'react';

export function useHabits() {
  const setHabits = useHabitStore((s) => s.setHabits);

  const query = useQuery({
    queryKey: ['habits'],
    queryFn: habitApi.getAll,
  });

  useEffect(() => {
    if (query.data) {
      setHabits(query.data);
    }
  }, [query.data]);

  return query;
}

export function useHabit(id: number) {
  return useQuery({
    queryKey: ['habit', id],
    queryFn: () => habitApi.getById(id),
  });
}

export function useCreateHabit() {
  const queryClient = useQueryClient();
  const addHabit = useHabitStore((s) => s.addHabit);

  return useMutation({
    mutationFn: (data: CreateHabitRequest) => habitApi.create(data),
    onSuccess: (habit) => {
      addHabit(habit);
      queryClient.invalidateQueries({ queryKey: ['habits'] });
    },
  });
}

export function useUpdateHabit() {
  const queryClient = useQueryClient();
  const updateHabit = useHabitStore((s) => s.updateHabit);

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<CreateHabitRequest> }) =>
      habitApi.update(id, data),
    onSuccess: (habit) => {
      updateHabit(habit);
      queryClient.invalidateQueries({ queryKey: ['habits'] });
      queryClient.invalidateQueries({ queryKey: ['habit', habit.id] });
    },
  });
}

export function useDeleteHabit() {
  const queryClient = useQueryClient();
  const removeHabit = useHabitStore((s) => s.removeHabit);

  return useMutation({
    mutationFn: (id: number) => habitApi.delete(id),
    onSuccess: (_, id) => {
      removeHabit(id);
      queryClient.invalidateQueries({ queryKey: ['habits'] });
    },
  });
}

export function useCompleteHabit() {
  const queryClient = useQueryClient();
  const toggleComplete = useHabitStore((s) => s.toggleComplete);

  return useMutation({
    mutationFn: ({ id, date }: { id: number; date: string }) =>
      habitApi.complete(id, date),
    onMutate: ({ id }) => {
      toggleComplete(id); // optimistic update
    },
    onError: (_, { id }) => {
      toggleComplete(id); // rollback
    },
    onSettled: (_, __, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['habits'] });
      queryClient.invalidateQueries({ queryKey: ['completions', id] });
    },
  });
}

export function useUncompleteHabit() {
  const queryClient = useQueryClient();
  const toggleComplete = useHabitStore((s) => s.toggleComplete);

  return useMutation({
    mutationFn: ({ id, date }: { id: number; date: string }) =>
      habitApi.uncomplete(id, date),
    onMutate: ({ id }) => {
      toggleComplete(id);
    },
    onError: (_, { id }) => {
      toggleComplete(id);
    },
    onSettled: (_, __, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['habits'] });
      queryClient.invalidateQueries({ queryKey: ['completions', id] });
    },
  });
}

export function useHabitStats(id: number) {
  return useQuery({
    queryKey: ['habitStats', id],
    queryFn: () => habitApi.getStats(id),
  });
}

export function useHabitHeatmap(id: number, year?: number) {
  return useQuery({
    queryKey: ['habitHeatmap', id, year],
    queryFn: () => habitApi.getHeatmap(id, year),
  });
}
