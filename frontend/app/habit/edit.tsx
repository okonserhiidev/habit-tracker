import { useState, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  ActivityIndicator,
} from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { useHabit, useUpdateHabit } from '../../hooks/useHabits';
import ColorPicker from '../../components/ColorPicker';
import Colors from '../../constants/Colors';

const FREQUENCIES = ['DAILY', 'WEEKLY', 'CUSTOM'] as const;
const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'] as const;
const DAY_SHORT: Record<string, string> = {
  MONDAY: 'Mon', TUESDAY: 'Tue', WEDNESDAY: 'Wed', THURSDAY: 'Thu',
  FRIDAY: 'Fri', SATURDAY: 'Sat', SUNDAY: 'Sun',
};

export default function EditHabitScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const habitId = Number(id);
  const router = useRouter();

  const { data: habit, isLoading } = useHabit(habitId);
  const updateMutation = useUpdateHabit();

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [color, setColor] = useState('#6C63FF');
  const [frequency, setFrequency] = useState<'DAILY' | 'WEEKLY' | 'CUSTOM'>('DAILY');
  const [customDays, setCustomDays] = useState<string[]>([]);
  const [identityText, setIdentityText] = useState('');
  const [miniVersion, setMiniVersion] = useState('');
  const [showAdvanced, setShowAdvanced] = useState(false);

  useEffect(() => {
    if (habit) {
      setName(habit.name);
      setDescription(habit.description ?? '');
      setColor(habit.color);
      setFrequency(habit.frequency);
      setCustomDays(habit.customDays ?? []);
      setIdentityText(habit.identityText ?? '');
      setMiniVersion(habit.miniVersion ?? '');
      // Default showAdvanced to true if habit already has these fields
      if (habit.identityText || habit.miniVersion) {
        setShowAdvanced(true);
      }
    }
  }, [habit]);

  const toggleDay = (day: string) => {
    setCustomDays((prev) =>
      prev.includes(day) ? prev.filter((d) => d !== day) : [...prev, day]
    );
  };

  const handleSave = async () => {
    if (!name.trim()) return;
    if (frequency === 'CUSTOM' && customDays.length === 0) return;
    await updateMutation.mutateAsync({
      id: habitId,
      data: {
        name: name.trim(),
        description: description.trim() || undefined,
        color,
        frequency,
        customDays: frequency === 'CUSTOM' ? customDays : undefined,
        identityText: identityText.trim() || undefined,
        miniVersion: miniVersion.trim() || undefined,
      },
    });
    router.back();
  };

  if (isLoading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color={Colors.primary} />
      </View>
    );
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
        <Text style={styles.backText}>← Cancel</Text>
      </TouchableOpacity>

      <Text style={styles.title}>Edit Habit</Text>

      <View style={styles.field}>
        <Text style={styles.label}>Name *</Text>
        <TextInput
          style={styles.input}
          placeholder="e.g. Morning run"
          placeholderTextColor="#9CA3AF"
          value={name}
          onChangeText={setName}
        />
      </View>

      <View style={styles.field}>
        <Text style={styles.label}>Description</Text>
        <TextInput
          style={[styles.input, styles.textArea]}
          placeholder="Optional description"
          placeholderTextColor="#9CA3AF"
          value={description}
          onChangeText={setDescription}
          multiline
          numberOfLines={3}
        />
      </View>

      <View style={styles.field}>
        <Text style={styles.label}>Color</Text>
        <ColorPicker value={color} onChange={setColor} />
      </View>

      <View style={styles.field}>
        <Text style={styles.label}>Frequency</Text>
        <View style={styles.freqRow}>
          {FREQUENCIES.map((f) => (
            <TouchableOpacity
              key={f}
              style={[
                styles.freqButton,
                frequency === f && { backgroundColor: color },
              ]}
              onPress={() => setFrequency(f)}
            >
              <Text
                style={[
                  styles.freqText,
                  frequency === f && styles.freqTextActive,
                ]}
              >
                {f === 'DAILY' ? 'Daily' : f === 'WEEKLY' ? 'Weekly' : 'Custom'}
              </Text>
            </TouchableOpacity>
          ))}
        </View>
      </View>

      {/* Custom days picker */}
      {frequency === 'CUSTOM' && (
        <View style={styles.field}>
          <Text style={styles.label}>Days of the Week</Text>
          <View style={styles.daysRow}>
            {DAYS.map((day) => {
              const selected = customDays.includes(day);
              return (
                <TouchableOpacity
                  key={day}
                  style={[styles.dayButton, selected && { backgroundColor: color }]}
                  onPress={() => toggleDay(day)}
                >
                  <Text style={[styles.dayText, selected && styles.dayTextActive]}>
                    {DAY_SHORT[day]}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>
          {customDays.length === 0 && (
            <Text style={styles.hint}>Select at least one day</Text>
          )}
        </View>
      )}

      {/* Advanced toggle */}
      <TouchableOpacity
        style={styles.advancedToggle}
        onPress={() => setShowAdvanced((v) => !v)}
        activeOpacity={0.7}
      >
        <Text style={styles.advancedToggleText}>
          {showAdvanced ? '▲ Advanced (Identity & Mini habits)' : '＋ Advanced (Identity & Mini habits)'}
        </Text>
      </TouchableOpacity>

      {showAdvanced && (
        <>
          <View style={styles.field}>
            <Text style={styles.label}>Who do I want to become?</Text>
            <Text style={styles.hint}>From "Atomic Habits": focus on identity, not results</Text>
            <TextInput
              style={styles.input}
              placeholder="e.g. I am a runner"
              placeholderTextColor="#9CA3AF"
              value={identityText}
              onChangeText={setIdentityText}
            />
          </View>

          <View style={styles.field}>
            <Text style={styles.label}>2-Minute Version</Text>
            <Text style={styles.hint}>What's the smallest version of this habit?</Text>
            <TextInput
              style={styles.input}
              placeholder="e.g. Put on running shoes"
              placeholderTextColor="#9CA3AF"
              value={miniVersion}
              onChangeText={setMiniVersion}
            />
          </View>
        </>
      )}

      <TouchableOpacity
        style={[styles.submitButton, { backgroundColor: color }, !name.trim() && styles.buttonDisabled]}
        onPress={handleSave}
        disabled={updateMutation.isPending || !name.trim() || (frequency === 'CUSTOM' && customDays.length === 0)}
      >
        {updateMutation.isPending ? (
          <ActivityIndicator color="#fff" />
        ) : (
          <Text style={styles.submitText}>Save Changes</Text>
        )}
      </TouchableOpacity>
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
    gap: 20,
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
  title: {
    fontSize: 28,
    fontWeight: '700',
    color: Colors.light.text,
  },
  field: {
    gap: 8,
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: Colors.light.text,
  },
  hint: {
    fontSize: 12,
    color: Colors.light.textSecondary,
    fontStyle: 'italic',
  },
  input: {
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: Colors.light.border,
    borderRadius: 12,
    padding: 14,
    fontSize: 16,
    color: Colors.light.text,
  },
  textArea: {
    minHeight: 80,
    textAlignVertical: 'top',
  },
  freqRow: {
    flexDirection: 'row',
    gap: 8,
  },
  freqButton: {
    flex: 1,
    paddingVertical: 12,
    borderRadius: 10,
    alignItems: 'center',
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: Colors.light.border,
  },
  freqText: {
    fontSize: 14,
    fontWeight: '600',
    color: Colors.light.textSecondary,
  },
  freqTextActive: {
    color: '#fff',
  },
  daysRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 6,
  },
  dayButton: {
    paddingHorizontal: 10,
    paddingVertical: 8,
    borderRadius: 8,
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: Colors.light.border,
  },
  dayText: {
    fontSize: 13,
    fontWeight: '600',
    color: Colors.light.textSecondary,
  },
  dayTextActive: {
    color: '#fff',
  },
  advancedToggle: {
    paddingVertical: 12,
    paddingHorizontal: 14,
    borderRadius: 10,
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: Colors.light.border,
    alignItems: 'center',
  },
  advancedToggleText: {
    fontSize: 14,
    fontWeight: '600',
    color: Colors.light.textSecondary,
  },
  submitButton: {
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
    marginTop: 8,
  },
  buttonDisabled: {
    opacity: 0.5,
  },
  submitText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
});
