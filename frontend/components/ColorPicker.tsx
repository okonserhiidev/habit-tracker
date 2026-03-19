import { useState } from 'react';
import { View, Text, StyleSheet, TextInput } from 'react-native';
import Colors from '../constants/Colors';

interface ColorPickerProps {
  value: string;
  onChange: (color: string) => void;
}

const PRESET_COLORS = [
  '#F44336', '#E91E63', '#9C27B0', '#673AB7',
  '#3F51B5', '#2196F3', '#03A9F4', '#00BCD4',
  '#009688', '#4CAF50', '#8BC34A', '#CDDC39',
  '#FFEB3B', '#FFC107', '#FF9800', '#FF5722',
];

function hexToRgb(hex: string): { r: number; g: number; b: number } {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  return result
    ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16),
      }
    : { r: 108, g: 99, b: 255 };
}

function rgbToHex(r: number, g: number, b: number): string {
  return (
    '#' +
    [r, g, b]
      .map((x) => Math.max(0, Math.min(255, x)).toString(16).padStart(2, '0'))
      .join('')
  );
}

export default function ColorPicker({ value, onChange }: ColorPickerProps) {
  const rgb = hexToRgb(value);
  const [r, setR] = useState(rgb.r);
  const [g, setG] = useState(rgb.g);
  const [b, setB] = useState(rgb.b);

  const updateColor = (nr: number, ng: number, nb: number) => {
    setR(nr);
    setG(ng);
    setB(nb);
    onChange(rgbToHex(nr, ng, nb));
  };

  return (
    <View style={styles.container}>
      {/* Preview */}
      <View style={[styles.preview, { backgroundColor: value }]}>
        <Text style={styles.previewText}>{value.toUpperCase()}</Text>
      </View>

      {/* Preset colors */}
      <View style={styles.presets}>
        {PRESET_COLORS.map((color) => (
          <View
            key={color}
            style={[
              styles.presetWrapper,
              value.toUpperCase() === color && styles.presetSelected,
            ]}
          >
            <Text
              style={[styles.preset, { backgroundColor: color }]}
              onPress={() => {
                const c = hexToRgb(color);
                updateColor(c.r, c.g, c.b);
              }}
            >
              {' '}
            </Text>
          </View>
        ))}
      </View>

      {/* RGB sliders */}
      <View style={styles.rgbRow}>
        <Text style={[styles.rgbLabel, { color: '#F44336' }]}>R</Text>
        <TextInput
          style={styles.rgbInput}
          keyboardType="number-pad"
          value={String(r)}
          onChangeText={(v) => updateColor(Number(v) || 0, g, b)}
          maxLength={3}
        />
        <Text style={[styles.rgbLabel, { color: '#4CAF50' }]}>G</Text>
        <TextInput
          style={styles.rgbInput}
          keyboardType="number-pad"
          value={String(g)}
          onChangeText={(v) => updateColor(r, Number(v) || 0, b)}
          maxLength={3}
        />
        <Text style={[styles.rgbLabel, { color: '#2196F3' }]}>B</Text>
        <TextInput
          style={styles.rgbInput}
          keyboardType="number-pad"
          value={String(b)}
          onChangeText={(v) => updateColor(r, g, Number(v) || 0)}
          maxLength={3}
        />
      </View>

      {/* Hex input */}
      <View style={styles.hexRow}>
        <Text style={styles.hexLabel}>HEX</Text>
        <TextInput
          style={styles.hexInput}
          value={value}
          onChangeText={(v) => {
            if (/^#[0-9a-fA-F]{6}$/.test(v)) {
              const c = hexToRgb(v);
              updateColor(c.r, c.g, c.b);
            }
          }}
          maxLength={7}
          autoCapitalize="characters"
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: 16,
  },
  preview: {
    height: 48,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },
  previewText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '700',
    textShadowColor: 'rgba(0,0,0,0.3)',
    textShadowRadius: 2,
  },
  presets: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  presetWrapper: {
    borderRadius: 16,
    padding: 2,
  },
  presetSelected: {
    borderWidth: 2,
    borderColor: Colors.light.text,
  },
  preset: {
    width: 32,
    height: 32,
    borderRadius: 16,
  },
  rgbRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  rgbLabel: {
    fontSize: 16,
    fontWeight: '700',
    width: 16,
  },
  rgbInput: {
    flex: 1,
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: Colors.light.border,
    borderRadius: 8,
    padding: 10,
    fontSize: 16,
    textAlign: 'center',
    color: Colors.light.text,
  },
  hexRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  hexLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: Colors.light.textSecondary,
  },
  hexInput: {
    flex: 1,
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: Colors.light.border,
    borderRadius: 8,
    padding: 10,
    fontSize: 16,
    color: Colors.light.text,
  },
});
