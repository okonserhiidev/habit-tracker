package com.habittracker.service;

import com.habittracker.exception.GlobalExceptionHandler.NotFoundException;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompletionServiceTest {

    @Mock private HabitCompletionRepository completionRepository;
    @Mock private HabitRepository habitRepository;
    @Mock private StreakService streakService;
    @Mock private AchievementService achievementService;
    @InjectMocks private CompletionService completionService;

    private Habit habit;
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        User user = User.builder().id(1L).build();
        habit = Habit.builder().id(1L).user(user).name("Бег").color("#E91E63")
                .frequency("DAILY").position(0).isArchived(false).build();
    }

    @Test
    void complete_success_returnsStreakAndPerfectDay() {
        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(habit));
        when(completionRepository.findByHabitIdAndCompletedDate(1L, today)).thenReturn(Optional.empty());
        when(completionRepository.save(any())).thenReturn(new HabitCompletion());
        when(streakService.calculateCurrentStreak(1L)).thenReturn(3);
        when(achievementService.checkPerfectDay(1L, today)).thenReturn(true);

        Map<String, Object> result = completionService.complete(1L, today, 1L);

        assertThat(result.get("currentStreak")).isEqualTo(3);
        assertThat(result.get("perfectDay")).isEqualTo(true);
        verify(completionRepository).save(any(HabitCompletion.class));
        verify(achievementService).checkStreakAchievements(1L, 1L, 3);
    }

    @Test
    void complete_habitNotFound_throwsException() {
        when(habitRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> completionService.complete(99L, today, 1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void complete_alreadyCompleted_throwsException() {
        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(habit));
        when(completionRepository.findByHabitIdAndCompletedDate(1L, today))
                .thenReturn(Optional.of(new HabitCompletion()));

        assertThatThrownBy(() -> completionService.complete(1L, today, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Already completed for this date");
    }

    @Test
    void uncomplete_success_deletesCompletion() {
        HabitCompletion completion = HabitCompletion.builder().id(1L).build();
        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(habit));
        when(completionRepository.findByHabitIdAndCompletedDate(1L, today))
                .thenReturn(Optional.of(completion));

        completionService.uncomplete(1L, today, 1L);

        verify(completionRepository).delete(completion);
    }

    @Test
    void uncomplete_notCompleted_throwsException() {
        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(habit));
        when(completionRepository.findByHabitIdAndCompletedDate(1L, today))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> completionService.uncomplete(1L, today, 1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getCompletions_returnsDates() {
        HabitCompletion c1 = HabitCompletion.builder().completedDate(today.minusDays(1)).build();
        HabitCompletion c2 = HabitCompletion.builder().completedDate(today).build();

        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(habit));
        when(completionRepository.findByHabitIdAndCompletedDateBetweenOrderByCompletedDateAsc(
                1L, today.minusDays(7), today))
                .thenReturn(List.of(c1, c2));

        List<LocalDate> result = completionService.getCompletions(1L, today.minusDays(7), today, 1L);

        assertThat(result).hasSize(2);
        assertThat(result).contains(today, today.minusDays(1));
    }

    @Test
    void getCompletions_habitNotFound_throwsException() {
        when(habitRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> completionService.getCompletions(99L, today.minusDays(7), today, 1L))
                .isInstanceOf(NotFoundException.class);
    }
}
