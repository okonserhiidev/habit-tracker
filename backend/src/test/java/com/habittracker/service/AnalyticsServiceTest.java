package com.habittracker.service;

import com.habittracker.dto.response.StatsResponse;
import com.habittracker.model.Habit;
import com.habittracker.model.HabitCompletion;
import com.habittracker.model.User;
import com.habittracker.repository.HabitCompletionRepository;
import com.habittracker.repository.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private HabitCompletionRepository completionRepository;
    @Mock private HabitRepository habitRepository;
    @Mock private StreakService streakService;
    @InjectMocks private AnalyticsService analyticsService;

    private Habit habit;
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        User user = User.builder().id(1L).build();
        habit = Habit.builder().id(1L).user(user).name("Бег").color("#E91E63")
                .frequency("DAILY").position(0).isArchived(false).build();
    }

    @Test
    void getHabitStats_returnsAllFields() {
        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(habit));
        when(streakService.calculateCurrentStreak(1L)).thenReturn(3);
        when(streakService.calculateBestStreak(1L)).thenReturn(10);
        when(completionRepository.countByHabitId(1L)).thenReturn(25L);

        HabitCompletion c1 = HabitCompletion.builder().completedDate(today).build();
        HabitCompletion c2 = HabitCompletion.builder().completedDate(today.minusDays(1)).build();

        when(completionRepository.findByHabitIdAndCompletedDateBetweenOrderByCompletedDateAsc(
                eq(1L), any(LocalDate.class), eq(today)))
                .thenReturn(List.of(c1, c2));

        StatsResponse stats = analyticsService.getHabitStats(1L, 1L);

        assertThat(stats.getCurrentStreak()).isEqualTo(3);
        assertThat(stats.getBestStreak()).isEqualTo(10);
        assertThat(stats.getTotalCompletions()).isEqualTo(25L);
        assertThat(stats.getCompletionRateWeek()).isGreaterThan(0);
        assertThat(stats.getCompletionsByDayOfWeek()).isNotNull();
        assertThat(stats.getCompletionsByDayOfWeek()).hasSize(7);
    }

    @Test
    void getHabitStats_allDaysOfWeekPresent() {
        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(habit));
        when(streakService.calculateCurrentStreak(1L)).thenReturn(0);
        when(streakService.calculateBestStreak(1L)).thenReturn(0);
        when(completionRepository.countByHabitId(1L)).thenReturn(0L);
        when(completionRepository.findByHabitIdAndCompletedDateBetweenOrderByCompletedDateAsc(
                eq(1L), any(LocalDate.class), eq(today)))
                .thenReturn(List.of());

        StatsResponse stats = analyticsService.getHabitStats(1L, 1L);

        for (DayOfWeek day : DayOfWeek.values()) {
            assertThat(stats.getCompletionsByDayOfWeek()).containsKey(day.name());
        }
    }

    @Test
    void getHabitStats_habitNotFound_throwsException() {
        when(habitRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> analyticsService.getHabitStats(99L, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getHeatmapData_returnsDates() {
        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(habit));

        HabitCompletion c1 = HabitCompletion.builder().completedDate(today).build();
        HabitCompletion c2 = HabitCompletion.builder().completedDate(today.minusDays(5)).build();

        when(completionRepository.findByHabitIdAndCompletedDateBetweenOrderByCompletedDateAsc(
                eq(1L), any(LocalDate.class), eq(today)))
                .thenReturn(List.of(c2, c1));

        List<LocalDate> heatmap = analyticsService.getHeatmapData(1L, 1L);

        assertThat(heatmap).hasSize(2);
        assertThat(heatmap).contains(today, today.minusDays(5));
    }

    @Test
    void getHeatmapData_noCompletions_returnsEmptyList() {
        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(habit));
        when(completionRepository.findByHabitIdAndCompletedDateBetweenOrderByCompletedDateAsc(
                eq(1L), any(LocalDate.class), eq(today)))
                .thenReturn(List.of());

        List<LocalDate> heatmap = analyticsService.getHeatmapData(1L, 1L);

        assertThat(heatmap).isEmpty();
    }
}
