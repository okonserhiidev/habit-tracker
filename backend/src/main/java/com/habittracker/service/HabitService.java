package com.habittracker.service;

import com.habittracker.dto.request.HabitRequest;
import com.habittracker.dto.response.HabitResponse;
import com.habittracker.exception.GlobalExceptionHandler.NotFoundException;
import com.habittracker.model.Habit;
import com.habittracker.model.User;
import com.habittracker.repository.HabitCompletionRepository;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;
    private final HabitCompletionRepository completionRepository;
    private final UserRepository userRepository;
    private final StreakService streakService;

    public List<HabitResponse> getHabits(Long userId) {
        List<Habit> habits = habitRepository.findByUserIdAndIsArchivedFalseOrderByPositionAsc(userId);
        LocalDate today = LocalDate.now();

        return habits.stream().map(habit -> {
            HabitResponse response = HabitResponse.from(habit);
            response.setCompletedToday(
                    completionRepository.findByHabitIdAndCompletedDate(habit.getId(), today).isPresent());
            response.setCurrentStreak(streakService.calculateCurrentStreak(habit.getId()));
            return response;
        }).toList();
    }

    public HabitResponse getHabit(Long habitId, Long userId) {
        Habit habit = findHabitByUser(habitId, userId);
        HabitResponse response = HabitResponse.from(habit);
        response.setCompletedToday(
                completionRepository.findByHabitIdAndCompletedDate(habit.getId(), LocalDate.now()).isPresent());
        response.setCurrentStreak(streakService.calculateCurrentStreak(habit.getId()));
        return response;
    }

    @Transactional
    public HabitResponse createHabit(HabitRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        int position = habitRepository.countByUserIdAndIsArchivedFalse(userId);

        Habit habit = Habit.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .color(request.getColor())
                .icon(request.getIcon())
                .category(request.getCategory())
                .identityText(request.getIdentityText())
                .miniVersion(request.getMiniVersion())
                .frequency(request.getFrequency() != null ? request.getFrequency() : "DAILY")
                .customDays(request.getCustomDays())
                .reminderTime(request.getReminderTime() != null ? LocalTime.parse(request.getReminderTime()) : null)
                .position(position)
                .isArchived(false)
                .build();

        habit = habitRepository.save(habit);
        return HabitResponse.from(habit);
    }

    @Transactional
    public HabitResponse updateHabit(Long habitId, HabitRequest request, Long userId) {
        Habit habit = findHabitByUser(habitId, userId);

        habit.setName(request.getName());
        habit.setDescription(request.getDescription());
        habit.setColor(request.getColor());
        habit.setIcon(request.getIcon());
        habit.setCategory(request.getCategory());
        habit.setIdentityText(request.getIdentityText());
        habit.setMiniVersion(request.getMiniVersion());
        if (request.getFrequency() != null) habit.setFrequency(request.getFrequency());
        habit.setCustomDays(request.getCustomDays());
        habit.setReminderTime(request.getReminderTime() != null ? LocalTime.parse(request.getReminderTime()) : null);

        habit = habitRepository.save(habit);
        return HabitResponse.from(habit);
    }

    @Transactional
    public void deleteHabit(Long habitId, Long userId) {
        Habit habit = findHabitByUser(habitId, userId);
        habitRepository.delete(habit);
    }

    @Transactional
    public HabitResponse archiveHabit(Long habitId, Long userId) {
        Habit habit = findHabitByUser(habitId, userId);
        habit.setIsArchived(true);
        habit = habitRepository.save(habit);
        return HabitResponse.from(habit);
    }

    @Transactional
    public void reorderHabits(List<Long> habitIds, Long userId) {
        for (int i = 0; i < habitIds.size(); i++) {
            Habit habit = findHabitByUser(habitIds.get(i), userId);
            habit.setPosition(i);
            habitRepository.save(habit);
        }
    }

    private Habit findHabitByUser(Long habitId, Long userId) {
        return habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new NotFoundException("Habit not found"));
    }
}
