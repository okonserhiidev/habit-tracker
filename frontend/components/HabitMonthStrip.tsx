import { View, Text, StyleSheet } from 'react-native';
import { useQuery } from '@tanstack/react-query';
import { habitApi } from '../api/habitApi';

const now = new Date();
const YEAR = now.getFullYear();
const MONTH = now.getMonth();
const TODAY = now.getDate();
const DAYS_IN_MONTH = new Date(YEAR, MONTH + 1, 0).getDate();
const pad = (n: number) => String(n).padStart(2, '0');
const START = `${YEAR}-${pad(MONTH + 1)}-01`;
const END = `${YEAR}-${pad(MONTH + 1)}-${pad(DAYS_IN_MONTH)}`;

// Monday-first offset (0=Mon … 6=Sun)
const FIRST_DOW = new Date(YEAR, MONTH, 1).getDay();
const OFFSET = (FIRST_DOW + 6) % 7;

const DAY_LABELS = ['M', 'T', 'W', 'T', 'F', 'S', 'S'];

interface Props {
  habitId: number;
  color: string;
}

export default function HabitMonthStrip({ habitId, color }: Props) {
  const { data: completions } = useQuery({
    queryKey: ['completions', habitId, 'month'],
    queryFn: () => habitApi.getCompletions(habitId, START, END),
    staleTime: 30_000,
  });

  const completedDays = new Set<number>();
  if (Array.isArray(completions)) {
    completions.forEach((c: any) => {
      const dateStr: string = c.completedDate ?? c.date ?? String(c);
      completedDays.add(parseInt(dateStr.split('-')[2], 10));
    });
  }

  const rate = TODAY > 0 ? Math.round((completedDays.size / TODAY) * 100) : 0;

  // Build a flat array of cells: nulls for offset, then day numbers
  const totalCells = OFFSET + DAYS_IN_MONTH;
  const weeks = Math.ceil(totalCells / 7);
  const cells: (number | null)[] = [
    ...Array(OFFSET).fill(null),
    ...Array.from({ length: DAYS_IN_MONTH }, (_, i) => i + 1),
  ];
  // Pad to full weeks
  while (cells.length < weeks * 7) cells.push(null);

  return (
    <View style={styles.container}>
      {/* Day-of-week header */}
      <View style={styles.weekRow}>
        {DAY_LABELS.map((lbl, i) => (
          <Text key={i} style={styles.dayLabel}>{lbl}</Text>
        ))}
      </View>

      {/* Calendar grid */}
      {Array.from({ length: weeks }, (_, w) => (
        <View key={w} style={styles.weekRow}>
          {cells.slice(w * 7, w * 7 + 7).map((day, i) => {
            const cellKey = w * 7 + i;
            if (day === null) {
              return <View key={cellKey} style={styles.cell} />;
            }
            const done = completedDays.has(day);
            const isToday = day === TODAY;
            const isFuture = day > TODAY;

            return (
              <View
                key={cellKey}
                style={[
                  styles.cell,
                  styles.dot,
                  done && { backgroundColor: color },
                  !done && !isFuture && styles.dotMissed,
                  !done && isToday && { borderWidth: 1.5, borderColor: color, backgroundColor: 'transparent' },
                  isFuture && styles.dotFuture,
                ]}
              />
            );
          })}
        </View>
      ))}

      {/* Rate */}
      <View style={styles.footer}>
        <Text style={[styles.rate, { color }]}>{completedDays.size}/{TODAY} дней — {rate}%</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginTop: 14,
    gap: 3,
  },
  weekRow: {
    flexDirection: 'row',
    gap: 3,
  },
  dayLabel: {
    width: 22,
    textAlign: 'center',
    fontSize: 10,
    fontWeight: '600',
    color: '#9CA3AF',
  },
  cell: {
    width: 22,
    height: 22,
  },
  dot: {
    borderRadius: 6,
    backgroundColor: 'transparent',
  },
  dotMissed: {
    backgroundColor: '#E5E7EB',
  },
  dotFuture: {
    backgroundColor: '#F3F4F6',
  },
  footer: {
    marginTop: 6,
    alignItems: 'flex-end',
  },
  rate: {
    fontSize: 11,
    fontWeight: '700',
  },
});
