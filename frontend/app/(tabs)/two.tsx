import { View, Text, StyleSheet, ScrollView, TouchableOpacity, Alert, ActivityIndicator } from 'react-native';
import { useState } from 'react';
import { useRouter } from 'expo-router';
import { useAuthStore } from '../../store/useAuthStore';
import Colors from '../../constants/Colors';
import { userApi } from '../../api/userApi';

const APP_VERSION = '1.0.0';

export default function SettingsScreen() {
  const { user, logout } = useAuthStore();
  const router = useRouter();
  const [isDeletingAccount, setIsDeletingAccount] = useState(false);

  const handleLogout = async () => {
    await logout();
    router.replace('/(auth)/login' as any);
  };

  const handleDeleteAccount = () => {
    Alert.alert(
      'Delete Account',
      'This will permanently delete your account and all your habits. This cannot be undone.',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            setIsDeletingAccount(true);
            try {
              await userApi.deleteAccount();
              await logout();
              router.replace('/(auth)/login' as any);
            } catch (error) {
              Alert.alert('Error', 'Failed to delete account. Please try again.');
            } finally {
              setIsDeletingAccount(false);
            }
          },
        },
      ]
    );
  };

  const initials = user?.name
    ? user.name.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2)
    : '?';

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.title}>Settings</Text>

      {/* Avatar + name */}
      <View style={styles.profileCard}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{initials}</Text>
        </View>
        <View style={styles.profileInfo}>
          <Text style={styles.profileName}>{user?.name ?? '—'}</Text>
          <Text style={styles.profileEmail}>{user?.email ?? '—'}</Text>
        </View>
      </View>

      {/* Account section */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Account</Text>
        <View style={styles.group}>
          <View style={styles.row}>
            <Text style={styles.rowIcon}>👤</Text>
            <Text style={styles.rowLabel}>Name</Text>
            <Text style={styles.rowValue}>{user?.name ?? '—'}</Text>
          </View>
          <View style={styles.separator} />
          <View style={styles.row}>
            <Text style={styles.rowIcon}>✉️</Text>
            <Text style={styles.rowLabel}>Email</Text>
            <Text style={styles.rowValue} numberOfLines={1}>{user?.email ?? '—'}</Text>
          </View>
        </View>
      </View>

      {/* App section */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>App</Text>
        <View style={styles.group}>
          <View style={styles.row}>
            <Text style={styles.rowIcon}>📱</Text>
            <Text style={styles.rowLabel}>Version</Text>
            <Text style={styles.rowValue}>{APP_VERSION}</Text>
          </View>
          <View style={styles.separator} />
          <View style={styles.row}>
            <Text style={styles.rowIcon}>🗄️</Text>
            <Text style={styles.rowLabel}>Backend</Text>
            <Text style={styles.rowValue}>Spring Boot 3.5</Text>
          </View>
        </View>
      </View>

      {/* Logout */}
      <TouchableOpacity style={styles.logoutButton} onPress={handleLogout}>
        <Text style={styles.logoutText}>🚪  Log Out</Text>
      </TouchableOpacity>

      {/* Delete Account */}
      <TouchableOpacity
        style={[styles.deleteButton, isDeletingAccount && styles.deleteButtonDisabled]}
        onPress={handleDeleteAccount}
        disabled={isDeletingAccount}
      >
        {isDeletingAccount ? (
          <ActivityIndicator color={Colors.danger} size="small" />
        ) : (
          <Text style={styles.deleteText}>Delete Account</Text>
        )}
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.light.background },
  content: { paddingHorizontal: 16, paddingTop: 60, paddingBottom: 40, gap: 0 },
  title: { fontSize: 28, fontWeight: '700', color: Colors.light.text, marginBottom: 24 },

  profileCard: {
    flexDirection: 'row', alignItems: 'center', backgroundColor: '#fff',
    borderRadius: 16, padding: 20, marginBottom: 28, gap: 16,
    shadowColor: '#000', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.06, shadowRadius: 4, elevation: 2,
  },
  avatar: {
    width: 56, height: 56, borderRadius: 28,
    backgroundColor: Colors.primary, justifyContent: 'center', alignItems: 'center',
  },
  avatarText: { color: '#fff', fontSize: 20, fontWeight: '700' },
  profileInfo: { flex: 1 },
  profileName: { fontSize: 18, fontWeight: '700', color: Colors.light.text },
  profileEmail: { fontSize: 13, color: Colors.light.textSecondary, marginTop: 2 },

  section: { marginBottom: 24 },
  sectionTitle: {
    fontSize: 13, fontWeight: '600', color: Colors.light.textSecondary,
    textTransform: 'uppercase', letterSpacing: 0.8, marginBottom: 10, paddingLeft: 4,
  },
  group: {
    backgroundColor: '#fff', borderRadius: 14,
    shadowColor: '#000', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.05, shadowRadius: 4, elevation: 1,
  },
  row: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16, paddingVertical: 14, gap: 12 },
  rowIcon: { fontSize: 18, width: 26, textAlign: 'center' },
  rowLabel: { flex: 1, fontSize: 16, color: Colors.light.text },
  rowValue: { fontSize: 15, color: Colors.light.textSecondary, maxWidth: 160 },
  separator: { height: 1, backgroundColor: Colors.light.border, marginLeft: 54 },

  logoutButton: {
    backgroundColor: '#FEE2E2', borderRadius: 14, padding: 16, alignItems: 'center', marginTop: 8,
  },
  logoutText: { color: Colors.danger, fontSize: 16, fontWeight: '600' },

  deleteButton: {
    borderWidth: 1, borderColor: Colors.danger, borderRadius: 14, padding: 16,
    alignItems: 'center', marginTop: 12, backgroundColor: 'transparent',
  },
  deleteButtonDisabled: { opacity: 0.5 },
  deleteText: { color: Colors.danger, fontSize: 16, fontWeight: '600' },
});
