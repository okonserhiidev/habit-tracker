import { useMemo } from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
} from 'react-native';
import { useRouter } from 'expo-router';
import { useHabits, useCompleteHabit, useUncompleteHabit } from '../../hooks/useHabits';
import { useHabitStore } from '../../store/useHabitStore';
import HabitCard from '../../components/HabitCard';
import ProgressBar from '../../components/ProgressBar';
import Colors from '../../constants/Colors';

export default function HomeScreen() {
  const router = useRouter();
  const { isLoading } = useHabits();
  const allHabits = useHabitStore((s) => s.habits);
  const habits = useMemo(() => allHabits.filter((h) => !h.isArchived), [allHabits]);
  const completeMutation = useCompleteHabit();
  const uncompleteMutation = useUncompleteHabit();

  const today = new Date().toISOString().split('T')[0];
  const completedCount = habits.filter((h) => h.completedToday).length;

  const handleToggle = (habitId: number, isCompleted: boolean | null) => {
    if (isCompleted) {
      uncompleteMutation.mutate({ id: habitId, date: today });
    } else {
      completeMutation.mutate({ id: habitId, date: today });
    }
  };

  if (isLoading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color={Colors.primary} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.greeting}>Today</Text>
        <Text style={styles.date}>
          {new Date().toLocaleDateString('en-US', {
            weekday: 'long',
            month: 'long',
            day: 'numeric',
          })}
        </Text>
      </View>

      {habits.length > 0 && (
        <ProgressBar completed={completedCount} total={habits.length} />
      )}

      <FlatList
        data={habits}
        keyExtractor={(item) => item.id.toString()}
        renderItem={({ item }) => (
          <HabitCard
            habit={item}
            onToggle={() => handleToggle(item.id, item.completedToday)}
            onPress={() => router.push(`/habit/${item.id}` as any)}
          />
        )}
        ListEmptyComponent={
          <View style={styles.empty}>
            <Text style={styles.emptyTitle}>No habits yet</Text>
            <Text style={styles.emptySubtitle}>
              Tap + to create your first habit
            </Text>
          </View>
        }
        contentContainerStyle={habits.length === 0 ? styles.emptyList : undefined}
        showsVerticalScrollIndicator={false}
      />

      <TouchableOpacity
        style={styles.fab}
        onPress={() => router.push('/habit/create' as any)}
      >
        <Text style={styles.fabText}>+</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.light.background,
    paddingHorizontal: 16,
    paddingTop: 60,
  },
  center: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: Colors.light.background,
  },
  header: {
    marginBottom: 20,
  },
  greeting: {
    fontSize: 28,
    fontWeight: '700',
    color: Colors.light.text,
  },
  date: {
    fontSize: 15,
    color: Colors.light.textSecondary,
    marginTop: 4,
  },
  empty: {
    alignItems: 'center',
    paddingTop: 80,
  },
  emptyTitle: {
    fontSize: 20,
    fontWeight: '600',
    color: Colors.light.text,
    marginBottom: 8,
  },
  emptySubtitle: {
    fontSize: 15,
    color: Colors.light.textSecondary,
  },
  emptyList: {
    flexGrow: 1,
  },
  fab: {
    position: 'absolute',
    right: 20,
    bottom: 100,
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: Colors.primary,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: Colors.primary,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 8,
  },
  fabText: {
    color: '#fff',
    fontSize: 28,
    fontWeight: '300',
    marginTop: -2,
  },
});
