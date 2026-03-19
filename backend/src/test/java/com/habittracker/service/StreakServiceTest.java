package com.habittracker.service;

import com.habittracker.model.FreezeDay;
import com.habittracker.repository.FreezeDayRepository;
import com.habittracker.repository.HabitCompletionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreakServiceTest {

    @Mock private HabitCompletionRepository completionRepository;
    @Mock private FreezeDayRepository freezeDayRepository;
    @InjectMocks private StreakService streakService;

    private final LocalDate today = LocalDate.now();

    private void stubNoFreezeDays() {
        when(freezeDayRepository.findByHabitId(1L)).thenReturn(List.of());
    }

    // === calculateCurrentStreak ===

    @Test
    void currentStreak_noCompletions_returnsZero() {
        when(completionRepository.findCompletedDatesByHabitIdDesc(1L)).thenReturn(List.of());
        stubNoFreezeDays();

        assertThat(streakService.calculateCurrentStreak(1L)).isEqualTo(0);
    }

    @Test
    void currentStreak_completedToday_returnsOne() {
        when(completionRepository.findCompletedDatesByHabitIdDesc(1L))
                .thenReturn(List.of(today));
        stubNoFreezeDays();

        assertThat(streakService.calculateCurrentStreak(1L)).isEqualTo(1);
    }

    @Test
    void currentStreak_threeDaysInARow_returnsThree() {
        when(completionRepository.findCompletedDatesByHabitIdDesc(1L))
                .thenReturn(List.of(today, today.minusDays(1), today.minusDays(2)));
        stubNoFreezeDays();

        assertThat(streakService.calculateCurrentStreak(1L)).isEqualTo(3);
    }

    @Test
    void currentStreak_gapInMiddle_returnsStreakFromToday() {
        when(completionRepository.findCompletedDatesByHabitIdDesc(1L))
                .thenReturn(List.of(today, today.minusDays(1), today.minusDays(3)));
        stubNoFreezeDays();

        assertThat(streakService.calculateCurrentStreak(1L)).isEqualTo(2);
    }

    @Test
    void currentStreak_notCompletedToday_startsFromYesterday() {
        when(completionRepository.findCompletedDatesByHabitIdDesc(1L))
                .thenReturn(List.of(today.minusDays(1), today.minusDays(2)));
        stubNoFreezeDays();

        assertThat(streakService.calculateCurrentStreak(1L)).isEqualTo(2);
    }

    @Test
    void currentStreak_notCompletedTodayOrYesterday_returnsZero() {
        when(completionRepository.findCompletedDatesByHabitIdDesc(1L))
                .thenReturn(List.of(today.minusDays(2), today.minusDays(3)));
        stubNoFreezeDays();

        assertThat(streakService.calculateCurrentStreak(1L)).isEqualTo(0);
    }

    @Test
    void currentStreak_withFreezeDay_streakContinues() {
        FreezeDay freeze = FreezeDay.builder().freezeDate(today.minusDays(1)).build();
        when(freezeDayRepository.findByHabitId(1L)).thenReturn(List.of(freeze));
        // today completed, yesterday frozen, 2 days ago completed
        when(completionRepository.findCompletedDatesByHabitIdDesc(1L))
                .thenReturn(List.of(today, today.minusDays(2)));

        assertThat(streakService.calculateCurrentStreak(1L)).isEqualTo(3);
    }

    @Test
    void currentStreak_frozenToday_countsAsDone() {
        FreezeDay freeze = FreezeDay.builder().freezeDate(today).build();
        when(freezeDayRepository.findByHabitId(1L)).thenReturn(List.of(freeze));
        when(completionRepository.findCompletedDatesByHabitIdDesc(1L))
                .thenReturn(List.of(today.minusDays(1)));

        assertThat(streakService.calculateCurrentStreak(1L)).isEqualTo(2);
    }

    // === calculateBestStreak ===

    @Test
    void bestStreak_noCompletions_returnsZero() {
        when(completionRepository.findCompletedDatesByHabitIdDesc(1L)).thenReturn(List.of());

        assertThat(streakService.calculateBestStreak(1L)).isEqualTo(0);
    }

    @Test
    void bestStreak_singleCompletion_returnsOne() {
        when(completionRepository.findCompletedDatesByHabitIdDesc(1L))
                .thenReturn(List.of(today));
        stubNoFreezeDays();

        assertThat(streakService.calculateBestStreak(1L)).isEqualTo(1);
    }

    @Test
    void bestStreak_preservedAfterGap() {
        // 5 days in a row, then gap, then 2 days
        when(completionRepository.findCompletedDatesByHabitIdDesc(1L))
                .thenReturn(List.of(
                        today, today.minusDays(1),
                        // gap at -3
                        today.minusDays(5), today.minusDays(6), today.minusDays(7),
                        today.minusDays(8), today.minusDays(9)
                ));
        stubNoFreezeDays();

        assertThat(streakService.calculateBestStreak(1L)).isEqualTo(5);
    }

    @Test
    void bestStreak_withFreezeDay_countsFrozenDaysInStreak() {
        FreezeDay freeze = FreezeDay.builder().freezeDate(today.minusDays(2)).build();
        when(freezeDayRepository.findByHabitId(1L)).thenReturn(List.of(freeze));
        // today, yesterday, frozen(-2), -3
        when(completionRepository.findCompletedDatesByHabitIdDesc(1L))
                .thenReturn(List.of(today, today.minusDays(1), today.minusDays(3)));

        assertThat(streakService.calculateBestStreak(1L)).isEqualTo(3);
    }
}
