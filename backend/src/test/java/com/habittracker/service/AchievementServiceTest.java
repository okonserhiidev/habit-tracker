package com.habittracker.service;

import com.habittracker.model.Achievement;
import com.habittracker.model.Habit;
import com.habittracker.model.HabitCompletion;
import com.habittracker.repository.AchievementRepository;
import com.habittracker.repository.HabitCompletionRepository;
import com.habittracker.repository.HabitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock private AchievementRepository achievementRepository;
    @Mock private HabitCompletionRepository completionRepository;
    @Mock private HabitRepository habitRepository;
    @InjectMocks private AchievementService achievementService;

    // === checkStreakAchievements ===

    @Test
    void checkStreakAchievements_streak7_createsAchievement() {
        when(achievementRepository.findByUserIdAndHabitIdAndType(1L, 1L, "STREAK_7"))
                .thenReturn(Optional.empty());

        achievementService.checkStreakAchievements(1L, 1L, 7);

        verify(achievementRepository).save(argThat(a ->
                a.getType().equals("STREAK_7") && a.getMilestoneDays() == 7));
    }

    @Test
    void checkStreakAchievements_streak21_createsMultipleAchievements() {
        when(achievementRepository.findByUserIdAndHabitIdAndType(anyLong(), anyLong(), anyString()))
                .thenReturn(Optional.empty());

        achievementService.checkStreakAchievements(1L, 1L, 21);

        // Should create STREAK_7 and STREAK_21
        verify(achievementRepository, times(2)).save(any(Achievement.class));
    }

    @Test
    void checkStreakAchievements_alreadyUnlocked_doesNotCreateDuplicate() {
        when(achievementRepository.findByUserIdAndHabitIdAndType(1L, 1L, "STREAK_7"))
                .thenReturn(Optional.of(new Achievement()));

        achievementService.checkStreakAchievements(1L, 1L, 7);

        verify(achievementRepository, never()).save(any());
    }

    @Test
    void checkStreakAchievements_streak5_noAchievement() {
        achievementService.checkStreakAchievements(1L, 1L, 5);

        verify(achievementRepository, never()).save(any());
    }

    // === checkPerfectDay ===

    @Test
    void checkPerfectDay_allCompleted_returnsTrue() {
        Habit h1 = Habit.builder().id(1L).build();
        Habit h2 = Habit.builder().id(2L).build();
        when(habitRepository.findByUserIdAndIsArchivedFalseOrderByPositionAsc(1L))
                .thenReturn(List.of(h1, h2));

        HabitCompletion c1 = new HabitCompletion();
        HabitCompletion c2 = new HabitCompletion();
        when(completionRepository.findByUserIdAndDate(1L, LocalDate.now()))
                .thenReturn(List.of(c1, c2));
        when(achievementRepository.findByUserIdAndHabitIdIsNullAndType(1L, "PERFECT_DAY"))
                .thenReturn(Optional.empty());

        boolean result = achievementService.checkPerfectDay(1L, LocalDate.now());

        assertThat(result).isTrue();
        verify(achievementRepository).save(any(Achievement.class));
    }

    @Test
    void checkPerfectDay_notAllCompleted_returnsFalse() {
        Habit h1 = Habit.builder().id(1L).build();
        Habit h2 = Habit.builder().id(2L).build();
        when(habitRepository.findByUserIdAndIsArchivedFalseOrderByPositionAsc(1L))
                .thenReturn(List.of(h1, h2));
        when(completionRepository.findByUserIdAndDate(1L, LocalDate.now()))
                .thenReturn(List.of(new HabitCompletion())); // only 1 of 2

        boolean result = achievementService.checkPerfectDay(1L, LocalDate.now());

        assertThat(result).isFalse();
    }

    @Test
    void checkPerfectDay_noHabits_returnsFalse() {
        when(habitRepository.findByUserIdAndIsArchivedFalseOrderByPositionAsc(1L))
                .thenReturn(List.of());

        boolean result = achievementService.checkPerfectDay(1L, LocalDate.now());

        assertThat(result).isFalse();
    }

    // === getUserAchievements ===

    @Test
    void getUserAchievements_returnsListFromRepo() {
        Achievement a = Achievement.builder().id(1L).type("STREAK_7").build();
        when(achievementRepository.findByUserIdOrderByUnlockedAtDesc(1L))
                .thenReturn(List.of(a));

        List<Achievement> result = achievementService.getUserAchievements(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo("STREAK_7");
    }
}
