package com.habittracker.service;

import com.habittracker.model.FreezeDay;
import com.habittracker.model.Habit;
import com.habittracker.model.User;
import com.habittracker.repository.FreezeDayRepository;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FreezeDayServiceTest {

    @Mock private FreezeDayRepository freezeDayRepository;
    @Mock private HabitRepository habitRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private FreezeDayService freezeDayService;

    private User testUser;
    private Habit testHabit;
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("test@test.com").authProvider(User.AuthProvider.LOCAL).build();
        testHabit = Habit.builder().id(1L).user(testUser).name("Running").color("#4CAF50").build();
    }

    // === freezeDay ===

    @Test
    void freezeDay_success_returnsFreezeDay() {
        LocalDate yesterday = today.minusDays(1);
        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testHabit));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(freezeDayRepository.countByUserIdAndFreezeDateBetween(eq(1L), any(), any())).thenReturn(0L);
        when(freezeDayRepository.findByUserIdAndFreezeDateBetween(eq(1L), eq(yesterday), eq(yesterday))).thenReturn(List.of());
        when(freezeDayRepository.save(any(FreezeDay.class))).thenAnswer(inv -> {
            FreezeDay fd = inv.getArgument(0);
            fd.setId(1L);
            return fd;
        });

        FreezeDay result = freezeDayService.freezeDay(1L, 1L, yesterday);

        assertThat(result.getFreezeDate()).isEqualTo(yesterday);
        assertThat(result.getHabit().getId()).isEqualTo(1L);
        verify(freezeDayRepository).save(any(FreezeDay.class));
    }

    @Test
    void freezeDay_habitNotFound_throwsException() {
        when(habitRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> freezeDayService.freezeDay(99L, 1L, today))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Habit not found");
    }

    @Test
    void freezeDay_futureDate_throwsException() {
        LocalDate tomorrow = today.plusDays(1);
        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testHabit));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> freezeDayService.freezeDay(1L, 1L, tomorrow))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot freeze a future date");
    }

    @Test
    void freezeDay_weeklyLimitReached_throwsException() {
        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testHabit));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(freezeDayRepository.countByUserIdAndFreezeDateBetween(eq(1L), any(), any())).thenReturn(2L);

        assertThatThrownBy(() -> freezeDayService.freezeDay(1L, 1L, today.minusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Freeze day limit reached");

        verify(freezeDayRepository, never()).save(any());
    }

    @Test
    void freezeDay_alreadyFrozen_throwsException() {
        LocalDate yesterday = today.minusDays(1);
        FreezeDay existing = FreezeDay.builder().id(1L).habit(testHabit).freezeDate(yesterday).build();

        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testHabit));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(freezeDayRepository.countByUserIdAndFreezeDateBetween(eq(1L), any(), any())).thenReturn(0L);
        when(freezeDayRepository.findByUserIdAndFreezeDateBetween(1L, yesterday, yesterday)).thenReturn(List.of(existing));

        assertThatThrownBy(() -> freezeDayService.freezeDay(1L, 1L, yesterday))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This day is already frozen for this habit");
    }

    // === getRemainingFreezes ===

    @Test
    void getRemainingFreezes_noneUsed_returnsTwo() {
        when(freezeDayRepository.countByUserIdAndFreezeDateBetween(eq(1L), any(), any())).thenReturn(0L);

        assertThat(freezeDayService.getRemainingFreezes(1L)).isEqualTo(2);
    }

    @Test
    void getRemainingFreezes_oneUsed_returnsOne() {
        when(freezeDayRepository.countByUserIdAndFreezeDateBetween(eq(1L), any(), any())).thenReturn(1L);

        assertThat(freezeDayService.getRemainingFreezes(1L)).isEqualTo(1);
    }

    @Test
    void getRemainingFreezes_allUsed_returnsZero() {
        when(freezeDayRepository.countByUserIdAndFreezeDateBetween(eq(1L), any(), any())).thenReturn(2L);

        assertThat(freezeDayService.getRemainingFreezes(1L)).isEqualTo(0);
    }
}
