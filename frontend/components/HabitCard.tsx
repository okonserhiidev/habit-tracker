import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { Habit } from '../api/habitApi';
import Colors from '../constants/Colors';

interface HabitCardProps {
  habit: Habit;
  onToggle: () => void;
  onPress: () => void;
}

function getFrequencyLabel(habit: Habit): string {
  if (habit.frequency === 'DAILY') return 'Daily';
  if (habit.frequency === 'WEEKLY') return 'Weekly';
  if (habit.frequency === 'CUSTOM') {
    if (habit.customDays && habit.customDays.length > 0) {
      return habit.customDays.map((d: string) => d.slice(0, 3)).join(', ');
    }
    return 'Custom';
  }
  return '';
}

export default function HabitCard({ habit, onToggle, onPress }: HabitCardProps) {
  const completed = habit.completedToday;
  const freqLabel = getFrequencyLabel(habit);

  return (
    <TouchableOpacity
      style={[styles.card, { borderLeftColor: habit.color }]}
      onPress={onPress}
      activeOpacity={0.7}
    >
      <View style={[styles.topRow, completed && styles.topRowCompleted]}>
        <TouchableOpacity
          style={[
            styles.checkbox,
            { borderColor: habit.color },
            completed && { backgroundColor: habit.color },
          ]}
          onPress={onToggle}
          hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
        >
          {completed && <Text style={styles.checkmark}>✓</Text>}
        </TouchableOpacity>

        <View style={styles.content}>
          <Text style={styles.name}>{habit.name}</Text>
          {habit.description && (
            <Text style={styles.description} numberOfLines={1}>
              {habit.description}
            </Text>
          )}
          {freqLabel ? (
            <Text style={styles.frequency}>{freqLabel}</Text>
          ) : null}
        </View>

        {(habit.currentStreak ?? 0) > 0 && (
          <View style={styles.streakBadge}>
            <Text style={styles.streakIcon}>🔥</Text>
            <Text style={styles.streakCount}>{habit.currentStreak}</Text>
          </View>
        )}
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: 'column',
    backgroundColor: '#fff',
    borderRadius: 16,
    padding: 18,
    marginBottom: 12,
    borderLeftWidth: 4,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  topRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  topRowCompleted: {
    opacity: 0.45,
  },
  checkbox: {
    width: 34,
    height: 34,
    borderRadius: 17,
    borderWidth: 2.5,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 14,
  },
  checkmark: {
    color: '#fff',
    fontSize: 18,
    fontWeight: '700',
  },
  content: {
    flex: 1,
  },
  name: {
    fontSize: 18,
    fontWeight: '700',
    color: Colors.light.text,
  },
  description: {
    fontSize: 13,
    color: Colors.light.textSecondary,
    marginTop: 2,
  },
  frequency: {
    fontSize: 12,
    color: Colors.light.textSecondary,
    marginTop: 3,
  },
  streakBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFF3E0',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  streakIcon: {
    fontSize: 12,
  },
  streakCount: {
    fontSize: 13,
    fontWeight: '700',
    color: Colors.streak,
    marginLeft: 2,
  },
});
