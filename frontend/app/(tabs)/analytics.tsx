import { View, Text, ScrollView, StyleSheet, ActivityIndicator } from 'react-native';
import { useQuery } from '@tanstack/react-query';
import {
  analyticsApi,
  DashboardResponse,
  TrendsResponse,
  AchievementResponse,
  HabitSummary,
} from '../../api/analyticsApi';
import Colors from '../../constants/Colors';

const ACHIEVEMENT_META: Record<string, { icon: string; label: string }> = {
  STREAK_7:   { icon: '🔥', label: '7-Day Streak' },
  STREAK_21:  { icon: '💪', label: '21-Day Streak' },
  STREAK_30:  { icon: '🌟', label: '30-Day Streak' },
  STREAK_66:  { icon: '🚀', label: '66-Day Streak' },
  STREAK_100: { icon: '👑', label: '100-Day Streak' },
  PERFECT_DAY: { icon: '⭐', label: 'Perfect Day' },
};

function TrendArrow({ delta }: { delta: number }) {
  if (delta > 0) return <Text style={styles.deltaUp}>↑ +{Math.round(delta)}%</Text>;
  if (delta < 0) return <Text style={styles.deltaDown}>↓ {Math.round(delta)}%</Text>;
  return <Text style={styles.deltaNeutral}>→ 0%</Text>;
}

function RateBar({ rate, color }: { rate: number; color: string }) {
  return (
    <View style={styles.barTrack}>
      <View style={[styles.barFill, { width: `${Math.min(rate, 100)}%`, backgroundColor: color }]} />
    </View>
  );
}

function HabitRow({ habit }: { habit: HabitSummary }) {
  return (
    <View style={styles.habitRow}>
      <View style={[styles.habitDot, { backgroundColor: habit.color }]} />
      <Text style={styles.habitRowName} numberOfLines={1}>{habit.name}</Text>
      <RateBar rate={habit.completionRate} color={habit.color} />
      <Text style={[styles.habitRowRate, { color: habit.color }]}>
        {Math.round(habit.completionRate)}%
      </Text>
      {habit.currentStreak > 0 && (
        <Text style={styles.habitRowStreak}>🔥{habit.currentStreak}</Text>
      )}
    </View>
  );
}

function MiniTrendChart({ data }: { data: { rate: number }[] }) {
  const max = Math.max(...data.map((d) => d.rate), 1);
  return (
    <View style={styles.trendChart}>
      {data.map((point, i) => (
        <View
          key={i}
          style={[
            styles.trendBar,
            {
              height: Math.max(2, (point.rate / max) * 40),
              backgroundColor: point.rate > 66 ? Colors.success : point.rate > 33 ? Colors.warning : '#E5E7EB',
            },
          ]}
        />
      ))}
    </View>
  );
}

export default function AnalyticsScreen() {
  const { data: dashboard, isLoading: dashLoading } = useQuery({
    queryKey: ['dashboard'],
    queryFn: analyticsApi.getDashboard,
    staleTime: 60_000,
  });

  const { data: trends, isLoading: trendsLoading } = useQuery({
    queryKey: ['trends'],
    queryFn: analyticsApi.getTrends,
    staleTime: 60_000,
  });

  const { data: achievements } = useQuery({
    queryKey: ['achievements'],
    queryFn: analyticsApi.getAchievements,
    staleTime: 60_000,
  });

  const isLoading = dashLoading || trendsLoading;

  if (isLoading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color={Colors.primary} />
      </View>
    );
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
      <Text style={styles.title}>Analytics</Text>

      {/* Summary Cards */}
      <View style={styles.summaryGrid}>
        <View style={[styles.summaryCard, styles.summaryCardPrimary]}>
          <Text style={styles.summaryValue}>{dashboard?.activeHabits ?? 0}</Text>
          <Text style={styles.summaryLabelLight}>Active Habits</Text>
        </View>
        <View style={styles.summaryCard}>
          <Text style={[styles.summaryValue, { color: Colors.primary }]}>
            {Math.round(dashboard?.overallCompletionRate ?? 0)}%
          </Text>
          <Text style={styles.summaryLabel}>This Week</Text>
        </View>
        <View style={styles.summaryCard}>
          <Text style={[styles.summaryValue, { color: Colors.success }]}>
            {dashboard?.perfectDays ?? 0}
          </Text>
          <Text style={styles.summaryLabel}>Perfect Days</Text>
        </View>
        <View style={styles.summaryCard}>
          <Text style={[styles.summaryValue, { color: Colors.streak }]}>
            🔥 {dashboard?.perfectDayStreak ?? 0}
          </Text>
          <Text style={styles.summaryLabel}>Day Streak</Text>
        </View>
      </View>

      {/* Trends */}
      {trends && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Trends</Text>

          <View style={styles.card}>
            <View style={styles.trendRow}>
              <View style={styles.trendBlock}>
                <Text style={styles.trendLabel}>This Week</Text>
                <Text style={styles.trendValue}>{Math.round(trends.currentWeekRate)}%</Text>
                <TrendArrow delta={trends.changePercent} />
              </View>
              <View style={styles.trendDivider} />
              <View style={styles.trendBlock}>
                <Text style={styles.trendLabel}>This Month</Text>
                <Text style={styles.trendValue}>{Math.round(trends.currentMonthRate)}%</Text>
                <TrendArrow delta={trends.monthChangePercent} />
              </View>
            </View>

            {trends.dailyTrend?.length > 0 && (
              <>
                <Text style={[styles.trendLabel, { marginTop: 16, marginBottom: 8 }]}>
                  Last {trends.dailyTrend.length} Days
                </Text>
                <MiniTrendChart data={trends.dailyTrend} />
              </>
            )}
          </View>
        </View>
      )}

      {/* Day of Week breakdown */}
      {dashboard?.completionByDayOfWeek && Object.keys(dashboard.completionByDayOfWeek).length > 0 && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>By Day of Week</Text>
          <View style={styles.card}>
            {Object.entries(dashboard.completionByDayOfWeek).map(([day, rate]) => (
              <View key={day} style={styles.dowRow}>
                <Text style={styles.dowLabel}>{day.slice(0, 3)}</Text>
                <RateBar rate={Number(rate)} color={Colors.primary} />
                <Text style={styles.dowRate}>{Math.round(Number(rate))}%</Text>
              </View>
            ))}
          </View>
        </View>
      )}

      {/* Top Performers */}
      {dashboard?.topHabits && dashboard.topHabits.length > 0 && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Top Performers</Text>
          <View style={styles.card}>
            {dashboard.topHabits.map((h) => (
              <HabitRow key={h.id} habit={h} />
            ))}
          </View>
        </View>
      )}

      {/* Needs Attention */}
      {dashboard?.needAttention && dashboard.needAttention.length > 0 && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Needs Attention</Text>
          <View style={styles.card}>
            {dashboard.needAttention.map((h) => (
              <HabitRow key={h.id} habit={h} />
            ))}
          </View>
        </View>
      )}

      {/* Achievements */}
      {achievements && achievements.length > 0 && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Achievements</Text>
          <View style={styles.achievementsGrid}>
            {achievements.map((a) => {
              const meta = ACHIEVEMENT_META[a.type] ?? { icon: '🎯', label: a.type };
              return (
                <View key={a.id} style={styles.achievementBadge}>
                  <Text style={styles.achievementIcon}>{meta.icon}</Text>
                  <Text style={styles.achievementLabel}>{meta.label}</Text>
                </View>
              );
            })}
          </View>
        </View>
      )}

      {!dashboard && !trends && (
        <View style={styles.empty}>
          <Text style={styles.emptyIcon}>📊</Text>
          <Text style={styles.emptyTitle}>No data yet</Text>
          <Text style={styles.emptySubtitle}>Complete some habits to see your analytics</Text>
        </View>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.light.background },
  content: { paddingHorizontal: 16, paddingTop: 60, paddingBottom: 40, gap: 0 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: Colors.light.background },
  title: { fontSize: 28, fontWeight: '700', color: Colors.light.text, marginBottom: 20 },

  summaryGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10, marginBottom: 20 },
  summaryCard: {
    flex: 1, minWidth: '44%', backgroundColor: '#fff', borderRadius: 14,
    padding: 16, alignItems: 'center',
    shadowColor: '#000', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.05, shadowRadius: 4, elevation: 1,
  },
  summaryCardPrimary: { backgroundColor: Colors.primary },
  summaryValue: { fontSize: 26, fontWeight: '700', color: Colors.light.text },
  summaryLabel: { fontSize: 12, color: Colors.light.textSecondary, marginTop: 4, textAlign: 'center' },
  summaryLabelLight: { fontSize: 12, color: 'rgba(255,255,255,0.8)', marginTop: 4 },

  section: { marginBottom: 20 },
  sectionTitle: { fontSize: 16, fontWeight: '700', color: Colors.light.text, marginBottom: 10 },
  card: {
    backgroundColor: '#fff', borderRadius: 14, padding: 16,
    shadowColor: '#000', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.05, shadowRadius: 4, elevation: 1,
  },

  trendRow: { flexDirection: 'row' },
  trendBlock: { flex: 1, alignItems: 'center', gap: 4 },
  trendDivider: { width: 1, backgroundColor: Colors.light.border, marginHorizontal: 12 },
  trendLabel: { fontSize: 12, color: Colors.light.textSecondary },
  trendValue: { fontSize: 28, fontWeight: '700', color: Colors.light.text },
  deltaUp: { fontSize: 13, fontWeight: '600', color: Colors.success },
  deltaDown: { fontSize: 13, fontWeight: '600', color: Colors.danger },
  deltaNeutral: { fontSize: 13, fontWeight: '600', color: Colors.light.textSecondary },

  trendChart: { flexDirection: 'row', alignItems: 'flex-end', gap: 2, height: 44 },
  trendBar: { flex: 1, borderRadius: 2 },

  barTrack: { flex: 1, height: 8, backgroundColor: Colors.light.border, borderRadius: 4, overflow: 'hidden' },
  barFill: { height: '100%', borderRadius: 4 },

  dowRow: { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 8 },
  dowLabel: { width: 32, fontSize: 12, fontWeight: '600', color: Colors.light.textSecondary },
  dowRate: { width: 36, fontSize: 12, fontWeight: '600', color: Colors.light.text, textAlign: 'right' },

  habitRow: { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 12 },
  habitDot: { width: 10, height: 10, borderRadius: 5 },
  habitRowName: { width: 100, fontSize: 13, fontWeight: '600', color: Colors.light.text },
  habitRowRate: { width: 36, fontSize: 13, fontWeight: '700', textAlign: 'right' },
  habitRowStreak: { fontSize: 12, minWidth: 30 },

  achievementsGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  achievementBadge: {
    backgroundColor: '#fff', borderRadius: 14, padding: 14, alignItems: 'center', width: '30%',
    shadowColor: '#000', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.05, shadowRadius: 4, elevation: 1,
  },
  achievementIcon: { fontSize: 28, marginBottom: 6 },
  achievementLabel: { fontSize: 11, fontWeight: '600', color: Colors.light.text, textAlign: 'center' },

  empty: { alignItems: 'center', paddingTop: 60 },
  emptyIcon: { fontSize: 48, marginBottom: 16 },
  emptyTitle: { fontSize: 20, fontWeight: '600', color: Colors.light.text, marginBottom: 8 },
  emptySubtitle: { fontSize: 14, color: Colors.light.textSecondary, textAlign: 'center' },
});
