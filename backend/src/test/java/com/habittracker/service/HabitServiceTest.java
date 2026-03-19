package com.habittracker.service;

import com.habittracker.dto.request.HabitRequest;
import com.habittracker.dto.response.HabitResponse;
import com.habittracker.exception.GlobalExceptionHandler.NotFoundException;
import com.habittracker.model.Habit;
import com.habittracker.model.User;
import com.habittracker.repository.HabitCompletionRepository;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitServiceTest {

    @Mock private HabitRepository habitRepository;
    @Mock private HabitCompletionRepository completionRepository;
    @Mock private UserRepository userRepository;
    @Mock private StreakService streakService;
    @InjectMocks private HabitService habitService;

    private User user;
    private Habit habit;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@test.com").build();
        habit = Habit.builder()
                .id(1L).user(user).name("Бег").color("#E91E63")
                .frequency("DAILY").position(0).isArchived(false)
                .build();
    }

    // === getHabits ===

    @Test
    void getHabits_returnsListWithStreakAndCompletion() {
        when(habitRepository.findByUserIdAndIsArchivedFalseOrderByPositionAsc(1L))
                .thenReturn(List.of(habit));
        when(completionRepository.findByHabitIdAndCompletedDate(1L, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(streakService.calculateCurrentStreak(1L)).thenReturn(5);

        List<HabitResponse> result = habitService.getHabits(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Бег");
        assertThat(result.get(0).getCompletedToday()).isFalse();
        assertThat(result.get(0).getCurrentStreak()).isEqualTo(5);
    }

    @Test
    void getHabits_emptyList_returnsEmpty() {
        when(habitRepository.findByUserIdAndIsArchivedFalseOrderByPositionAsc(1L))
                .thenReturn(List.of());

        List<HabitResponse> result = habitService.getHabits(1L);

        assertThat(result).isEmpty();
    }

    // === getHabit ===

    @Test
    void getHabit_existingHabit_returnsResponse() {
        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(habit));
        when(completionRepository.findByHabitIdAndCompletedDate(1L, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(streakService.calculateCurrentStreak(1L)).thenReturn(0);

        HabitResponse result = habitService.getHabit(1L, 1L);

        assertThat(result.getName()).isEqualTo("Бег");
    }

    @Test
    void getHabit_notFound_throwsException() {
        when(habitRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> habitService.getHabit(99L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Habit not found");
    }

    // === createHabit ===

    @Test
    void createHabit_success_returnsResponse() {
        HabitRequest request = new HabitRequest();
        request.setName("Чтение");
        request.setColor("#FF5733");
        request.setIdentityText("Я — читающий человек");
        request.setMiniVersion("1 страница");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(habitRepository.countByUserIdAndIsArchivedFalse(1L)).thenReturn(3);
        when(habitRepository.save(any(Habit.class))).thenAnswer(inv -> {
            Habit h = inv.getArgument(0);
            h.setId(2L);
            return h;
        });

        HabitResponse result = habitService.createHabit(request, 1L);

        assertThat(result.getName()).isEqualTo("Чтение");
        assertThat(result.getColor()).isEqualTo("#FF5733");
        assertThat(result.getIdentityText()).isEqualTo("Я — читающий человек");
        assertThat(result.getMiniVersion()).isEqualTo("1 страница");
        verify(habitRepository).save(argThat(h -> h.getPosition() == 3));
    }

    @Test
    void createHabit_defaultFrequency_isDaily() {
        HabitRequest request = new HabitRequest();
        request.setName("Тест");
        request.setColor("#000000");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(habitRepository.countByUserIdAndIsArchivedFalse(1L)).thenReturn(0);
        when(habitRepository.save(any(Habit.class))).thenAnswer(inv -> {
            Habit h = inv.getArgument(0);
            h.setId(1L);
            return h;
        });

        HabitResponse result = habitService.createHabit(request, 1L);

        assertThat(result.getFrequency()).isEqualTo("DAILY");
    }

    @Test
    void createHabit_userNotFound_throwsException() {
        HabitRequest request = new HabitRequest();
        request.setName("Тест");
        request.setColor("#000000");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> habitService.createHabit(request, 99L))
                .isInstanceOf(NotFoundException.class);
    }

    // === updateHabit ===

    @Test
    void updateHabit_success_updatesFields() {
        HabitRequest request = new HabitRequest();
        request.setName("Новое название");
        request.setColor("#FFFFFF");

        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(habit));
        when(habitRepository.save(any(Habit.class))).thenAnswer(inv -> inv.getArgument(0));

        HabitResponse result = habitService.updateHabit(1L, request, 1L);

        assertThat(result.getName()).isEqualTo("Новое название");
        assertThat(result.getColor()).isEqualTo("#FFFFFF");
    }

    @Test
    void updateHabit_notFound_throwsException() {
        HabitRequest request = new HabitRequest();
        request.setName("Test");
        request.setColor("#000000");

        when(habitRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> habitService.updateHabit(99L, request, 1L))
                .isInstanceOf(NotFoundException.class);
    }

    // === deleteHabit ===

    @Test
    void deleteHabit_success_deletesHabit() {
        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(habit));

        habitService.deleteHabit(1L, 1L);

        verify(habitRepository).delete(habit);
    }

    @Test
    void deleteHabit_notFound_throwsException() {
        when(habitRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> habitService.deleteHabit(99L, 1L))
                .isInstanceOf(NotFoundException.class);
    }

    // === archiveHabit ===

    @Test
    void archiveHabit_success_setsArchivedTrue() {
        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(habit));
        when(habitRepository.save(any(Habit.class))).thenAnswer(inv -> inv.getArgument(0));

        HabitResponse result = habitService.archiveHabit(1L, 1L);

        assertThat(result.getIsArchived()).isTrue();
    }

    // === reorderHabits ===

    @Test
    void reorderHabits_success_updatesPositions() {
        Habit habit2 = Habit.builder().id(2L).user(user).name("Медитация")
                .color("#9C27B0").frequency("DAILY").position(1).isArchived(false).build();

        when(habitRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(habit2));
        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(habit));
        when(habitRepository.save(any(Habit.class))).thenAnswer(inv -> inv.getArgument(0));

        habitService.reorderHabits(List.of(2L, 1L), 1L);

        verify(habitRepository).save(argThat(h -> h.getId().equals(2L) && h.getPosition() == 0));
        verify(habitRepository).save(argThat(h -> h.getId().equals(1L) && h.getPosition() == 1));
    }
}
