import { useEffect, useRef, useState } from 'react';
import { DarkTheme, DefaultTheme, ThemeProvider } from '@react-navigation/native';
import { useFonts } from 'expo-font';
import { Slot, useRouter } from 'expo-router';
import * as SplashScreen from 'expo-splash-screen';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { View, ActivityIndicator } from 'react-native';
import 'react-native-reanimated';

import { useColorScheme } from '@/components/useColorScheme';
import { useAuthStore } from '../store/useAuthStore';
import Colors from '../constants/Colors';
import { getItem } from '../utils/storage';

export { ErrorBoundary } from 'expo-router';

SplashScreen.preventAutoHideAsync();

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30_000,
    },
  },
});

function InitialRedirect() {
  const { isAuthenticated, isLoading, restoreSession } = useAuthStore();
  const router = useRouter();
  const didRedirect = useRef(false);
  const [checkingOnboarding, setCheckingOnboarding] = useState(true);

  useEffect(() => {
    restoreSession();
  }, []);

  useEffect(() => {
    let cancelled = false;
    getItem('onboardingCompleted').then((value) => {
      if (cancelled) return;
      if (!value) {
        router.replace('/onboarding' as any);
      }
      setCheckingOnboarding(false);
    });
    return () => { cancelled = true; };
  }, []);

  useEffect(() => {
    if (isLoading || checkingOnboarding || didRedirect.current) return;

    didRedirect.current = true;
    if (isAuthenticated) {
      router.replace('/(tabs)' as any);
    } else {
      router.replace('/(auth)/login' as any);
    }
  }, [isLoading, isAuthenticated, checkingOnboarding]);

  if (isLoading || checkingOnboarding) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#F8F9FA' }}>
        <ActivityIndicator size="large" color={Colors.primary} />
      </View>
    );
  }

  return <Slot />;
}

export default function RootLayout() {
  const [loaded, error] = useFonts({
    SpaceMono: require('../assets/fonts/SpaceMono-Regular.ttf'),
  });

  useEffect(() => {
    if (error) throw error;
  }, [error]);

  useEffect(() => {
    if (loaded) {
      SplashScreen.hideAsync();
    }
  }, [loaded]);

  if (!loaded) {
    return null;
  }

  const colorScheme = useColorScheme() ?? 'light';

  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider value={colorScheme === 'dark' ? DarkTheme : DefaultTheme}>
        <InitialRedirect />
      </ThemeProvider>
    </QueryClientProvider>
  );
}
