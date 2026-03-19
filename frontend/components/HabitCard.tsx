import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { Habit } from '../api/habitApi';
import Colors from '../constants/Colors';
import HabitMonthStrip from './HabitMonthStrip';

interface HabitCardProps {
  habit: Habit;
  onToggle: () => void;
  onPress: () => void;
}

export default function HabitCard({ habit, onToggle, onPress }: HabitCardProps) {
  const completed = habit.completedToday;

  return (
    <TouchableOpacity style={styles.card} onPress={onPress} activeOpacity={0.7}>
      <View style={styles.topRow}>
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
          <Text style={[styles.name, completed && styles.nameCompleted]}>
            {habit.name}
          </Text>
          {habit.description && (
            <Text style={styles.description} numberOfLines={1}>
              {habit.description}
            </Text>
          )}
        </View>

        {(habit.currentStreak ?? 0) > 0 && (
          <View style={styles.streakBadge}>
            <Text style={styles.streakIcon}>🔥</Text>
            <Text style={styles.streakCount}>{habit.currentStreak}</Text>
          </View>
        )}
      </View>

      <HabitMonthStrip habitId={habit.id} color={habit.color} />
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
  checkbox: {
    width: 34,
    height: 34,
    borderRadius: 10,
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
  nameCompleted: {
    textDecorationLine: 'line-through',
    color: Colors.light.textSecondary,
  },
  description: {
    fontSize: 13,
    color: Colors.light.textSecondary,
    marginTop: 2,
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
