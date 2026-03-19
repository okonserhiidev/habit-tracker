package com.habittracker.service;

import com.habittracker.dto.response.DashboardResponse;
import com.habittracker.dto.response.TrendsResponse;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardAndTrendsTest {

    @Mock private HabitCompletionRepository completionRepository;
    @Mock private HabitRepository habitRepository;
    @Mock private StreakService streakService;
    @InjectMocks private AnalyticsService analyticsService;

    private User testUser;
    private Habit habit1;
    private Habit habit2;
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("test@test.com").authProvider(User.AuthProvider.LOCAL).build();
        habit1 = Habit.builder().id(1L).user(testUser).name("Running").color("#4CAF50").frequency("DAILY").isArchived(false).build();
        habit2 = Habit.builder().id(2L).user(testUser).name("Reading").color("#2196F3").frequency("DAILY").isArchived(false).build();
    }

    // === Dashboard ===

    @Test
    void getDashboard_noHabits_returnsEmpty() {
        when(habitRepository.findByUserIdAndIsArchivedFalseOrderByPositionAsc(1L)).thenReturn(List.of());

        DashboardResponse result = analyticsService.getDashboard(1L);

        assertThat(result.getActiveHabits()).isEqualTo(0);
        assertThat(result.getOverallCompletionRate()).isEqualTo(0);
        assertThat(result.getTopHabits()).isEmpty();
    }

    @Test
    void getDashboard_withHabits_returnsStats() {
        when(habitRepository.findByUserIdAndIsArchivedFalseOrderByPositionAsc(1L))
                .thenReturn(List.of(habit1, habit2));

        // habit1: 4/7 completions this week
        HabitCompletion c1 = HabitCompletion.builder().completedDate(today).build();
        HabitCompletion c2 = HabitCompletion.builder().completedDate(today.minusDays(1)).build();
        HabitCompletion c3 = HabitCompletion.builder().completedDate(today.minusDays(2)).build();
        HabitCompletion c4 = HabitCompletion.builder().completedDate(today.minusDays(3)).build();
        when(completionRepository.findByHabitIdAndCompletedDateBetweenOrderByCompletedDateAsc(
                eq(1L), any(), any())).thenReturn(List.of(c1, c2, c3, c4));
        when(streakService.calculateCurrentStreak(1L)).thenReturn(4);

        // habit2: 1/7 completions
        HabitCompletion c5 = HabitCompletion.builder().completedDate(today).build();
        when(completionRepository.findByHabitIdAndCompletedDateBetweenOrderByCompletedDateAsc(
                eq(2L), any(), any())).thenReturn(List.of(c5));
        when(streakService.calculateCurrentStreak(2L)).thenReturn(1);

        // Perfect day check (today both completed)
        when(completionRepository.findByUserIdAndDate(eq(1L), any()))
                .thenReturn(List.of(c1, c5)); // both habits today

        DashboardResponse result = analyticsService.getDashboard(1L);

        assertThat(result.getActiveHabits()).isEqualTo(2);
        assertThat(result.getOverallCompletionRate()).isGreaterThan(0);
    }

    // === Trends ===

    @Test
    void getTrends_noHabits_returnsZeros() {
        when(habitRepository.findByUserIdAndIsArchivedFalseOrderByPositionAsc(1L)).thenReturn(List.of());

        TrendsResponse result = analyticsService.getTrends(1L);

        assertThat(result.getCurrentWeekRate()).isEqualTo(0);
        assertThat(result.getChangePercent()).isEqualTo(0);
        assertThat(result.getDailyTrend()).isEmpty();
    }

    @Test
    void getTrends_withData_returnsTrend() {
        when(habitRepository.findByUserIdAndIsArchivedFalseOrderByPositionAsc(1L))
                .thenReturn(List.of(habit1));

        // Current week: 3 completions
        HabitCompletion c1 = HabitCompletion.builder().completedDate(today).build();
        HabitCompletion c2 = HabitCompletion.builder().completedDate(today.minusDays(1)).build();
        HabitCompletion c3 = HabitCompletion.builder().completedDate(today.minusDays(2)).build();

        when(completionRepository.findByHabitIdAndCompletedDateBetweenOrderByCompletedDateAsc(
                eq(1L), any(), any()))
                .thenReturn(List.of(c1, c2, c3));

        when(completionRepository.findByUserIdAndDate(eq(1L), any()))
                .thenReturn(List.of()); // simplify

        TrendsResponse result = analyticsService.getTrends(1L);

        assertThat(result.getDailyTrend()).hasSize(30);
        assertThat(result.getCurrentWeekRate()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void getTrends_changePercent_calculatesCorrectly() {
        when(habitRepository.findByUserIdAndIsArchivedFalseOrderByPositionAsc(1L))
                .thenReturn(List.of(habit1));

        // Same completions for any range (simplified)
        when(completionRepository.findByHabitIdAndCompletedDateBetweenOrderByCompletedDateAsc(
                eq(1L), any(), any())).thenReturn(List.of());
        when(completionRepository.findByUserIdAndDate(eq(1L), any())).thenReturn(List.of());

        TrendsResponse result = analyticsService.getTrends(1L);

        // Both weeks 0% → change = 0
        assertThat(result.getChangePercent()).isEqualTo(0);
    }
}
