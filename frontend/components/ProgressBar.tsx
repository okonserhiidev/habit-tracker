import { View, Text, StyleSheet } from 'react-native';
import Colors from '../constants/Colors';

interface ProgressBarProps {
  completed: number;
  total: number;
}

export default function ProgressBar({ completed, total }: ProgressBarProps) {
  const progress = total > 0 ? completed / total : 0;
  const isPerfect = completed === total && total > 0;

  return (
    <View style={styles.container}>
      <View style={styles.textRow}>
        <Text style={styles.label}>
          {isPerfect ? 'Perfect Day!' : `${completed} of ${total}`}
        </Text>
        <Text style={styles.percentage}>{Math.round(progress * 100)}%</Text>
      </View>
      <View style={styles.track}>
        <View
          style={[
            styles.fill,
            {
              width: `${progress * 100}%`,
              backgroundColor: isPerfect ? Colors.success : Colors.primary,
            },
          ]}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: 16,
  },
  textRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 6,
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: Colors.light.text,
  },
  percentage: {
    fontSize: 14,
    fontWeight: '600',
    color: Colors.light.textSecondary,
  },
  track: {
    height: 8,
    backgroundColor: Colors.light.border,
    borderRadius: 4,
    overflow: 'hidden',
  },
  fill: {
    height: '100%',
    borderRadius: 4,
  },
});
