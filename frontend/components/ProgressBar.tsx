import { View, Text, StyleSheet } from 'react-native';
import Colors from '../constants/Colors';

interface ProgressBarProps {
  completed: number;
  total: number;
  label?: string;
}

export default function ProgressBar({ completed, total, label }: ProgressBarProps) {
  const progress = total > 0 ? completed / total : 0;
  const percentage = Math.round(progress * 100);

  return (
    <View style={styles.container}>
      <View style={styles.barRow}>
        <View style={styles.track}>
          <View
            style={[
              styles.fill,
              {
                width: `${percentage}%`,
                backgroundColor: Colors.primary,
              },
            ]}
          />
        </View>
        <Text style={styles.percentage}>{percentage}%</Text>
      </View>
      {label ? (
        <Text style={styles.label}>{label}</Text>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: 16,
  },
  barRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  track: {
    flex: 1,
    height: 10,
    backgroundColor: '#E5E7EB',
    borderRadius: 5,
    overflow: 'hidden',
  },
  fill: {
    height: '100%',
    borderRadius: 5,
  },
  percentage: {
    fontSize: 14,
    fontWeight: '700',
    color: Colors.primary,
    minWidth: 38,
    textAlign: 'right',
  },
  label: {
    fontSize: 13,
    color: Colors.light.textSecondary,
    marginTop: 5,
  },
});
