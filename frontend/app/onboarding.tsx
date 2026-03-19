import { useRef, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Dimensions,
  NativeSyntheticEvent,
  NativeScrollEvent,
} from 'react-native';
import { useRouter } from 'expo-router';
import Colors from '../constants/Colors';
import { setItem } from '../utils/storage';
import { useAuthStore } from '../store/useAuthStore';

const { width } = Dimensions.get('window');

const slides = [
  {
    emoji: '🌱',
    title: 'Build Better Habits',
    subtitle: 'Track your daily habits and build consistency with streaks',
  },
  {
    emoji: '🔥',
    title: 'Stay Consistent',
    subtitle: 'Check off habits every day and watch your streaks grow',
  },
  {
    emoji: '📊',
    title: 'See Your Progress',
    subtitle: 'Detailed analytics help you understand your patterns and improve',
  },
];

export default function OnboardingScreen() {
  const router = useRouter();
  const scrollRef = useRef<ScrollView>(null);
  const [currentIndex, setCurrentIndex] = useState(0);

  const handleScroll = (event: NativeSyntheticEvent<NativeScrollEvent>) => {
    const index = Math.round(event.nativeEvent.contentOffset.x / width);
    setCurrentIndex(index);
  };

  const goToNext = () => {
    const next = currentIndex + 1;
    scrollRef.current?.scrollTo({ x: next * width, animated: true });
    setCurrentIndex(next);
  };

  const finish = async () => {
    await setItem('onboardingCompleted', 'true');
    const { isAuthenticated } = useAuthStore.getState();
    if (isAuthenticated) {
      router.replace('/(tabs)' as any);
    } else {
      router.replace('/(auth)/login' as any);
    }
  };

  const isLastSlide = currentIndex === slides.length - 1;

  return (
    <View style={styles.container}>
      {/* Skip button */}
      {!isLastSlide && (
        <TouchableOpacity style={styles.skipButton} onPress={finish}>
          <Text style={styles.skipText}>Skip</Text>
        </TouchableOpacity>
      )}

      {/* Slides */}
      <ScrollView
        ref={scrollRef}
        horizontal
        pagingEnabled
        showsHorizontalScrollIndicator={false}
        onMomentumScrollEnd={handleScroll}
        scrollEventThrottle={16}
        style={styles.scrollView}
      >
        {slides.map((slide, index) => (
          <View key={index} style={styles.slide}>
            <View style={styles.card}>
              <Text style={styles.emoji}>{slide.emoji}</Text>
              <Text style={styles.title}>{slide.title}</Text>
              <Text style={styles.subtitle}>{slide.subtitle}</Text>
            </View>
          </View>
        ))}
      </ScrollView>

      {/* Dot indicators */}
      <View style={styles.dotsContainer}>
        {slides.map((_, index) => (
          <View
            key={index}
            style={[styles.dot, index === currentIndex && styles.dotActive]}
          />
        ))}
      </View>

      {/* Action button */}
      <View style={styles.buttonContainer}>
        {isLastSlide ? (
          <TouchableOpacity style={styles.primaryButton} onPress={finish}>
            <Text style={styles.primaryButtonText}>Get Started</Text>
          </TouchableOpacity>
        ) : (
          <TouchableOpacity style={styles.primaryButton} onPress={goToNext}>
            <Text style={styles.primaryButtonText}>Next</Text>
          </TouchableOpacity>
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.light.background,
  },
  skipButton: {
    position: 'absolute',
    top: 60,
    right: 24,
    zIndex: 10,
    padding: 8,
  },
  skipText: {
    fontSize: 16,
    color: Colors.light.textSecondary,
    fontWeight: '500',
  },
  scrollView: {
    flex: 1,
  },
  slide: {
    width,
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 32,
    paddingTop: 80,
  },
  card: {
    backgroundColor: '#fff',
    borderRadius: 24,
    padding: 40,
    alignItems: 'center',
    width: '100%',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.08,
    shadowRadius: 16,
    elevation: 4,
  },
  emoji: {
    fontSize: 72,
    marginBottom: 28,
  },
  title: {
    fontSize: 26,
    fontWeight: '700',
    color: Colors.light.text,
    textAlign: 'center',
    marginBottom: 16,
  },
  subtitle: {
    fontSize: 16,
    color: Colors.light.textSecondary,
    textAlign: 'center',
    lineHeight: 24,
  },
  dotsContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 24,
    gap: 8,
  },
  dot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: Colors.light.border,
  },
  dotActive: {
    width: 24,
    backgroundColor: Colors.primary,
  },
  buttonContainer: {
    paddingHorizontal: 24,
    paddingBottom: 48,
  },
  primaryButton: {
    backgroundColor: Colors.primary,
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
  },
  primaryButtonText: {
    color: '#fff',
    fontSize: 17,
    fontWeight: '600',
  },
});
