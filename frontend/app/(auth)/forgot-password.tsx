import { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity,
  StyleSheet, KeyboardAvoidingView, Platform, ActivityIndicator,
} from 'react-native';
import { useRouter } from 'expo-router';
import api from '../../api/client';
import Colors from '../../constants/Colors';

export default function ForgotPasswordScreen() {
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [sent, setSent] = useState(false);
  const [error, setError] = useState('');
  const router = useRouter();

  const handleSend = async () => {
    if (!email.trim()) return;
    setIsLoading(true);
    setError('');
    try {
      await api.post('/auth/password/reset-request', { email: email.trim() });
      setSent(true);
    } catch {
      setError('Could not send reset email. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  if (sent) {
    return (
      <View style={styles.container}>
        <View style={styles.successBox}>
          <Text style={styles.successIcon}>📬</Text>
          <Text style={styles.successTitle}>Check your email</Text>
          <Text style={styles.successText}>
            We sent a password reset link to {email}
          </Text>
          <TouchableOpacity style={styles.button} onPress={() => router.back()}>
            <Text style={styles.buttonText}>Back to Login</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <TouchableOpacity style={styles.backButton} onPress={() => router.back()}>
        <Text style={styles.backText}>← Back</Text>
      </TouchableOpacity>

      <View style={styles.header}>
        <Text style={styles.title}>Forgot Password?</Text>
        <Text style={styles.subtitle}>Enter your email and we'll send you a reset link</Text>
      </View>

      <View style={styles.form}>
        {error ? (
          <View style={styles.errorBox}>
            <Text style={styles.errorText}>{error}</Text>
          </View>
        ) : null}

        <TextInput
          style={styles.input}
          placeholder="Email address"
          placeholderTextColor="#9CA3AF"
          value={email}
          onChangeText={setEmail}
          keyboardType="email-address"
          autoCapitalize="none"
          autoCorrect={false}
        />

        <TouchableOpacity
          style={[styles.button, !email.trim() && styles.buttonDisabled]}
          onPress={handleSend}
          disabled={isLoading || !email.trim()}
        >
          {isLoading ? (
            <ActivityIndicator color="#fff" />
          ) : (
            <Text style={styles.buttonText}>Send Reset Link</Text>
          )}
        </TouchableOpacity>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.light.background, paddingHorizontal: 24, justifyContent: 'center' },
  backButton: { position: 'absolute', top: 60, left: 24, paddingVertical: 8 },
  backText: { fontSize: 16, color: Colors.primary },
  header: { alignItems: 'center', marginBottom: 40 },
  title: { fontSize: 28, fontWeight: '700', color: Colors.light.text, marginBottom: 10 },
  subtitle: { fontSize: 15, color: Colors.light.textSecondary, textAlign: 'center' },
  form: { gap: 16 },
  errorBox: { backgroundColor: '#FEE2E2', padding: 12, borderRadius: 8 },
  errorText: { color: Colors.danger, textAlign: 'center' },
  input: {
    backgroundColor: '#fff', borderWidth: 1, borderColor: Colors.light.border,
    borderRadius: 12, padding: 16, fontSize: 16, color: Colors.light.text,
  },
  button: { backgroundColor: Colors.primary, padding: 16, borderRadius: 12, alignItems: 'center' },
  buttonDisabled: { opacity: 0.5 },
  buttonText: { color: '#fff', fontSize: 16, fontWeight: '600' },
  successBox: { alignItems: 'center', gap: 16, paddingHorizontal: 16 },
  successIcon: { fontSize: 56 },
  successTitle: { fontSize: 24, fontWeight: '700', color: Colors.light.text },
  successText: { fontSize: 15, color: Colors.light.textSecondary, textAlign: 'center' },
});
