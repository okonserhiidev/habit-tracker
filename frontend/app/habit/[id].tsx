import { View, Text, ScrollView, StyleSheet, ActivityIndicator, TouchableOpacity, Alert } from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { useHabit, useHabitStats, useCompleteHabit, useUncompleteHabit, useDeleteHabit } from '../../hooks/useHabits';
import { habitApi } from '../../api/habitApi';
import { freezeApi } from '../../api/analyticsApi';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useHabitStore } from '../../store/useHabitStore';
import MonthCalendar from '../../components/MonthCalendar';
import Colors from '../../constants/Colors';
import { useMemo } from 'react';

export default function HabitDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const habitId = Number(id);
  const router = useRouter();

  const { data: habit, isLoading: habitLoading } = useHabit(habitId);
  const { data: stats, isLoading: statsLoading } = useHabitStats(habitId);

  const { data: completions } = useQuery({
    queryKey: ['completions', habitId],
    queryFn: () => habitApi.getCompletions(habitId, '2025-01-01', '2027-12-31'),
  });

  const completeMutation = useCompleteHabit();
  const uncompleteMutation = useUncompleteHabit();
  const deleteMutation = useDeleteHabit();
  const queryClient = useQueryClient();
  const removeHabit = useHabitStore((s) => s.removeHabit);

  const { data: freezeInfo } = useQuery({
    queryKey: ['freezeDays'],
    queryFn: freezeApi.getFreezeDays,
    staleTime: 30_000,
  });

  const freezeMutation = useMutation({
    mutationFn: () => {
      const today = new Date().toISOString().split('T')[0];
      return freezeApi.freeze(habitId, today);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['freezeDays'] });
      queryClient.invalidateQueries({ queryKey: ['habitStats', habitId] });
    },
  });

  const archiveMutation = useMutation({
    mutationFn: () => habitApi.archive(habitId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['habits'] });
      router.back();
    },
  });

  const completedDates = useMemo(() => {
    const set = new Set<string>();
    if (Array.isArray(completions)) {
      completions.forEach((c: any) => set.add(c.completedDate ?? c.date ?? c));
    }
    return set;
  }, [completions]);

  const handleEdit = () => {
    router.push(`/habit/edit?id=${habitId}` as any);
  };

  const handleArchive = () => {
    archiveMutation.mutate();
  };

  const handleDelete = () => {
    deleteMutation.mutate(habitId, {
      onSuccess: () => {
        router.back();
      },
    });
  };

  const handleToggleDate = (date: string) => {
    const onSettled = () => {
      queryClient.invalidateQueries({ queryKey: ['completions', habitId] });
      queryClient.invalidateQueries({ queryKey: ['habitStats', habitId] });
    };
    if (completedDates.has(date)) {
      uncompleteMutation.mutate({ id: habitId, date }, { onSettled });
    } else {
      completeMutation.mutate({ id: habitId, date }, { onSettled });
    }
  };

  if (habitLoading || statsLoading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color={Colors.primary} />
      </View>
    );
  }

  if (!habit) {
    return (
      <View style={styles.center}>
        <Text>Habit not found</Text>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
        <Text style={styles.backText}>← Back</Text>
      </TouchableOpacity>

      <View style={[styles.titleRow, { borderLeftColor: habit.color }]}>
        <Text style={styles.title}>{habit.name}</Text>
        {habit.description && (
          <Text style={styles.description}>{habit.description}</Text>
        )}
      </View>

      {/* Streak */}
      <View style={styles.statsRow}>
        <View style={styles.statCard}>
          <Text style={styles.statValue}>🔥 {stats?.currentStreak ?? 0}</Text>
          <Text style={styles.statLabel}>Current Streak</Text>
        </View>
        <View style={styles.statCard}>
          <Text style={styles.statValue}>{stats?.bestStreak ?? 0}</Text>
          <Text style={styles.statLabel}>Best Streak</Text>
        </View>
        <View style={styles.statCard}>
          <Text style={styles.statValue}>{stats?.totalCompletions ?? 0}</Text>
          <Text style={styles.statLabel}>Total Days</Text>
        </View>
      </View>

      {/* Completion Rate */}
      <View style={styles.ratesRow}>
        <View style={styles.rateCard}>
          <Text style={styles.rateValue}>
            {Math.round(stats?.completionRateWeek ?? 0)}%
          </Text>
          <Text style={styles.rateLabel}>This Week</Text>
        </View>
        <View style={styles.rateCard}>
          <Text style={styles.rateValue}>
            {Math.round(stats?.completionRateMonth ?? 0)}%
          </Text>
          <Text style={styles.rateLabel}>This Month</Text>
        </View>
      </View>

      {/* Calendar */}
      <MonthCalendar
        completedDates={completedDates}
        color={habit.color}
        onToggleDate={handleToggleDate}
      />

      {/* Days of week */}
      {stats?.completionsByDayOfWeek && (
        <View style={styles.weekChart}>
          <Text style={styles.sectionTitle}>By Day of Week</Text>
          {Object.entries(stats.completionsByDayOfWeek).map(([day, count]) => (
            <View key={day} style={styles.weekBar}>
              <Text style={styles.weekDay}>{day.slice(0, 3)}</Text>
              <View style={styles.barTrack}>
                <View
                  style={[
                    styles.barFill,
                    {
                      width: `${Math.min(
                        ((count as number) /
                          Math.max(
                            ...Object.values(stats.completionsByDayOfWeek).map(Number),
                            1
                          )) *
                          100,
                        100
                      )}%`,
                      backgroundColor: habit.color,
                    },
                  ]}
                />
              </View>
              <Text style={styles.weekCount}>{count as number}</Text>
            </View>
          ))}
        </View>
      )}

      {/* Identity text */}
      {habit.identityText && (
        <View style={styles.identityCard}>
          <Text style={styles.identityLabel}>I am becoming:</Text>
          <Text style={styles.identityText}>{habit.identityText}</Text>
        </View>
      )}

      {/* Actions */}
      <View style={styles.actionsSection}>
        <TouchableOpacity style={styles.editButton} onPress={handleEdit}>
          <Text style={styles.editButtonText}>✏️  Edit Habit</Text>
        </TouchableOpacity>

        {/* Freeze Day */}
        <TouchableOpacity
          style={[
            styles.freezeButton,
            (freezeInfo?.remainingThisWeek === 0 || freezeMutation.isPending) && styles.buttonDisabledOpacity,
          ]}
          onPress={() => freezeMutation.mutate()}
          disabled={freezeMutation.isPending || freezeInfo?.remainingThisWeek === 0}
        >
          {freezeMutation.isPending ? (
            <ActivityIndicator color="#3B82F6" />
          ) : (
            <Text style={styles.freezeButtonText}>
              ❄️  Freeze Today
              {freezeInfo !== undefined
                ? `  (${freezeInfo.remainingThisWeek} left this week)`
                : ''}
            </Text>
          )}
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.archiveButton}
          onPress={handleArchive}
          disabled={archiveMutation.isPending}
        >
          <Text style={styles.archiveButtonText}>📦  Archive Habit</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.deleteButton}
          onPress={handleDelete}
          disabled={deleteMutation.isPending}
        >
          {deleteMutation.isPending ? (
            <ActivityIndicator color={Colors.danger} />
          ) : (
            <Text style={styles.deleteButtonText}>🗑️  Delete Habit</Text>
          )}
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.light.background,
  },
  content: {
    paddingHorizontal: 16,
    paddingTop: 60,
    paddingBottom: 40,
    gap: 16,
  },
  center: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: Colors.light.background,
  },
  backButton: {
    paddingVertical: 4,
  },
  backText: {
    fontSize: 16,
    color: Colors.primary,
  },
  titleRow: {
    borderLeftWidth: 4,
    paddingLeft: 12,
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
    color: Colors.light.text,
  },
  description: {
    fontSize: 14,
    color: Colors.light.textSecondary,
    marginTop: 4,
  },
  statsRow: {
    flexDirection: 'row',
    gap: 8,
  },
  statCard: {
    flex: 1,
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
  },
  statValue: {
    fontSize: 22,
    fontWeight: '700',
    color: Colors.light.text,
  },
  statLabel: {
    fontSize: 12,
    color: Colors.light.textSecondary,
    marginTop: 4,
  },
  ratesRow: {
    flexDirection: 'row',
    gap: 8,
  },
  rateCard: {
    flex: 1,
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
  },
  rateValue: {
    fontSize: 20,
    fontWeight: '700',
    color: Colors.primary,
  },
  rateLabel: {
    fontSize: 12,
    color: Colors.light.textSecondary,
    marginTop: 4,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: Colors.light.text,
    marginBottom: 12,
  },
  weekChart: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
  },
  weekBar: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  weekDay: {
    width: 36,
    fontSize: 12,
    fontWeight: '600',
    color: Colors.light.textSecondary,
  },
  barTrack: {
    flex: 1,
    height: 16,
    backgroundColor: Colors.light.border,
    borderRadius: 8,
    overflow: 'hidden',
    marginHorizontal: 8,
  },
  barFill: {
    height: '100%',
    borderRadius: 8,
  },
  weekCount: {
    width: 24,
    fontSize: 12,
    fontWeight: '600',
    color: Colors.light.text,
    textAlign: 'right',
  },
  identityCard: {
    backgroundColor: '#EDE9FE',
    borderRadius: 12,
    padding: 16,
  },
  identityLabel: {
    fontSize: 12,
    fontWeight: '600',
    color: Colors.primaryDark,
    marginBottom: 4,
  },
  identityText: {
    fontSize: 16,
    fontWeight: '600',
    color: Colors.primaryDark,
  },
  actionsSection: {
    gap: 12,
    marginTop: 8,
    paddingBottom: 16,
  },
  freezeButton: {
    backgroundColor: '#EFF6FF',
    borderWidth: 1,
    borderColor: '#3B82F6',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
  },
  freezeButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#3B82F6',
  },
  buttonDisabledOpacity: {
    opacity: 0.4,
  },
  editButton: {
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: Colors.primary,
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
  },
  editButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: Colors.primary,
  },
  archiveButton: {
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: Colors.light.textSecondary,
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
  },
  archiveButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: Colors.light.textSecondary,
  },
  deleteButton: {
    backgroundColor: '#FEE2E2',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
  },
  deleteButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: Colors.danger,
  },
});
