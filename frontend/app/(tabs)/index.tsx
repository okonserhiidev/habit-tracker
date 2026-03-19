import { useMemo, useRef, useEffect } from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  ScrollView,
  Animated,
} from 'react-native';
import { useRouter } from 'expo-router';
import { useHabits, useCompleteHabit, useUncompleteHabit } from '../../hooks/useHabits';
import { useHabitStore } from '../../store/useHabitStore';
import { useAuthStore } from '../../store/useAuthStore';
import HabitCard from '../../components/HabitCard';
import ProgressBar from '../../components/ProgressBar';
import Colors from '../../constants/Colors';

const QUOTES = [
  "Small steps every day lead to big changes.",
  "You don't have to be great to start, but you have to start to be great.",
  "Consistency is the key to achieving and maintaining momentum.",
  "Every action you take is a vote for the type of person you wish to become.",
  "It's not about perfection, it's about progress.",
  "The secret of getting ahead is getting started.",
  "Believe you can and you're halfway there.",
];

function getGreeting(email: string | null | undefined): string {
  const hour = new Date().getHours();
  let timeGreeting = 'Good morning';
  if (hour >= 12 && hour < 17) timeGreeting = 'Good afternoon';
  else if (hour >= 17) timeGreeting = 'Good evening';

  if (!email) return timeGreeting;
  const username = email.split('@')[0];
  const displayName = username.charAt(0).toUpperCase() + username.slice(1);
  return `${timeGreeting}, ${displayName} 👋`;
}

export default function HomeScreen() {
  const router = useRouter();
  const { isLoading } = useHabits();
  const allHabits = useHabitStore((s) => s.habits);
  const habits = useMemo(() => allHabits.filter((h) => !h.isArchived), [allHabits]);
  const completeMutation = useCompleteHabit();
  const uncompleteMutation = useUncompleteHabit();
  const user = useAuthStore((s) => s.user);

  const today = new Date().toISOString().split('T')[0];
  const completedCount = habits.filter((h) => h.completedToday).length;
  const incompleteHabits = habits.filter((h) => !h.completedToday);
  const completedHabits = habits.filter((h) => h.completedToday);

  const greeting = getGreeting(user?.email ?? user?.name);
  const dailyQuote = QUOTES[new Date().getDay()];

  // Perfect Day animation
  const isPerfectDay = habits.length > 0 && completedCount === habits.length;
  const bannerAnim = useRef(new Animated.Value(-120)).current;
  const bannerShownRef = useRef(false);

  useEffect(() => {
    if (isPerfectDay && !bannerShownRef.current) {
      bannerShownRef.current = true;
      Animated.sequence([
        Animated.spring(bannerAnim, {
          toValue: 0,
          useNativeDriver: true,
          speed: 14,
          bounciness: 10,
        }),
        Animated.delay(2800),
        Animated.timing(bannerAnim, {
          toValue: -120,
          duration: 400,
          useNativeDriver: true,
        }),
      ]).start(() => {
        bannerShownRef.current = false;
      });
    }
  }, [isPerfectDay]);

  const progressLabel =
    habits.length > 0 && completedCount === habits.length
      ? 'All done! 🎉'
      : `${completedCount} of ${habits.length} done`;

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
      <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.scrollContent}>
        <View style={styles.header}>
          <Text style={styles.greeting}>{greeting}</Text>
          <Text style={styles.date}>
            {new Date().toLocaleDateString('en-US', {
              weekday: 'long',
              month: 'long',
              day: 'numeric',
            })}
          </Text>
        </View>

        {/* Daily motivational quote */}
        <View style={styles.quoteCard}>
          <Text style={styles.quoteText}>"{dailyQuote}"</Text>
        </View>

        {habits.length > 0 && (
          <ProgressBar
            completed={completedCount}
            total={habits.length}
            label={progressLabel}
          />
        )}

        {habits.length === 0 ? (
          <View style={styles.empty}>
            <Text style={styles.emptyTitle}>No habits yet</Text>
            <Text style={styles.emptySubtitle}>Tap + to create your first habit</Text>
          </View>
        ) : (
          <>
            {/* To Do section */}
            {incompleteHabits.length > 0 && (
              <>
                <Text style={styles.sectionHeader}>To Do</Text>
                {incompleteHabits.map((item) => (
                  <HabitCard
                    key={item.id.toString()}
                    habit={item}
                    onToggle={() => handleToggle(item.id, item.completedToday)}
                    onPress={() => router.push(`/habit/${item.id}` as any)}
                  />
                ))}
              </>
            )}

            {/* Completed section */}
            {completedCount > 0 && (
              <>
                <Text style={styles.sectionHeader}>Completed ✓</Text>
                {completedHabits.map((item) => (
                  <HabitCard
                    key={item.id.toString()}
                    habit={item}
                    onToggle={() => handleToggle(item.id, item.completedToday)}
                    onPress={() => router.push(`/habit/${item.id}` as any)}
                  />
                ))}
              </>
            )}
          </>
        )}

        {/* Bottom padding for FAB */}
        <View style={{ height: 80 }} />
      </ScrollView>

      <TouchableOpacity
        style={styles.fab}
        onPress={() => router.push('/habit/create' as any)}
      >
        <Text style={styles.fabText}>＋</Text>
      </TouchableOpacity>

      {/* Perfect Day banner */}
      <Animated.View
        style={[styles.perfectDayBanner, { transform: [{ translateY: bannerAnim }] }]}
        pointerEvents="none"
      >
        <Text style={styles.perfectDayEmoji}>🎉</Text>
        <View>
          <Text style={styles.perfectDayTitle}>Perfect Day!</Text>
          <Text style={styles.perfectDaySubtitle}>All habits complete</Text>
        </View>
        <Text style={styles.perfectDayEmoji}>🔥</Text>
      </Animated.View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.light.background,
  },
  scrollContent: {
    paddingHorizontal: 16,
    paddingTop: 60,
    paddingBottom: 20,
  },
  center: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: Colors.light.background,
  },
  header: {
    marginBottom: 16,
  },
  greeting: {
    fontSize: 26,
    fontWeight: '700',
    color: Colors.light.text,
  },
  date: {
    fontSize: 15,
    color: Colors.light.textSecondary,
    marginTop: 4,
  },
  quoteCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 14,
    marginBottom: 16,
    borderLeftWidth: 3,
    borderLeftColor: Colors.primary,
  },
  quoteText: {
    fontSize: 13,
    fontStyle: 'italic',
    color: Colors.light.textSecondary,
    lineHeight: 20,
  },
  sectionHeader: {
    fontSize: 15,
    fontWeight: '600',
    color: Colors.light.textSecondary,
    marginBottom: 10,
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
    fontSize: 26,
    fontWeight: '400',
  },
  perfectDayBanner: {
    position: 'absolute',
    top: 0,
    left: 16,
    right: 16,
    backgroundColor: Colors.primary,
    borderRadius: 16,
    marginTop: 52,
    paddingVertical: 14,
    paddingHorizontal: 20,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    shadowColor: Colors.primary,
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.4,
    shadowRadius: 12,
    elevation: 10,
    zIndex: 100,
  },
  perfectDayTitle: {
    color: '#fff',
    fontSize: 17,
    fontWeight: '700',
  },
  perfectDaySubtitle: {
    color: 'rgba(255,255,255,0.8)',
    fontSize: 13,
    marginTop: 1,
  },
  perfectDayEmoji: {
    fontSize: 28,
  },
});
