import { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  ActivityIndicator,
} from 'react-native';
import { useRouter } from 'expo-router';
import { useCreateHabit } from '../../hooks/useHabits';
import ColorPicker from '../../components/ColorPicker';
import Colors from '../../constants/Colors';

const FREQUENCIES = ['DAILY', 'WEEKLY', 'CUSTOM'] as const;
const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'] as const;
const DAY_SHORT: Record<string, string> = {
  MONDAY: 'Mon', TUESDAY: 'Tue', WEDNESDAY: 'Wed', THURSDAY: 'Thu',
  FRIDAY: 'Fri', SATURDAY: 'Sat', SUNDAY: 'Sun',
};

export default function CreateHabitScreen() {
  const router = useRouter();
  const createMutation = useCreateHabit();

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [color, setColor] = useState('#6C63FF');
  const [frequency, setFrequency] = useState<'DAILY' | 'WEEKLY' | 'CUSTOM'>('DAILY');
  const [customDays, setCustomDays] = useState<string[]>([]);
  const [identityText, setIdentityText] = useState('');
  const [miniVersion, setMiniVersion] = useState('');

  const toggleDay = (day: string) => {
    setCustomDays((prev) =>
      prev.includes(day) ? prev.filter((d) => d !== day) : [...prev, day]
    );
  };

  const handleCreate = async () => {
    if (!name.trim()) return;
    if (frequency === 'CUSTOM' && customDays.length === 0) return;

    await createMutation.mutateAsync({
      name: name.trim(),
      description: description.trim() || undefined,
      color,
      frequency,
      customDays: frequency === 'CUSTOM' ? customDays : undefined,
      identityText: identityText.trim() || undefined,
      miniVersion: miniVersion.trim() || undefined,
    });

    router.back();
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
        <Text style={styles.backText}>← Cancel</Text>
      </TouchableOpacity>

      <Text style={styles.title}>New Habit</Text>

      {/* Name */}
      <View style={styles.field}>
        <Text style={styles.label}>Name *</Text>
        <TextInput
          style={styles.input}
          placeholder="e.g. Morning run"
          placeholderTextColor="#9CA3AF"
          value={name}
          onChangeText={setName}
          autoFocus
        />
      </View>

      {/* Description */}
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

      {/* Color */}
      <View style={styles.field}>
        <Text style={styles.label}>Color</Text>
        <ColorPicker value={color} onChange={setColor} />
      </View>

      {/* Frequency */}
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

      {/* Identity (Atomic Habits) */}
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

      {/* Mini version (2-minute rule) */}
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

      {/* Submit */}
      <TouchableOpacity
        style={[styles.submitButton, { backgroundColor: color }, !name.trim() && styles.buttonDisabled]}
        onPress={handleCreate}
        disabled={createMutation.isPending || !name.trim() || (frequency === 'CUSTOM' && customDays.length === 0)}
      >
        {createMutation.isPending ? (
          <ActivityIndicator color="#fff" />
        ) : (
          <Text style={styles.submitText}>Create Habit</Text>
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
