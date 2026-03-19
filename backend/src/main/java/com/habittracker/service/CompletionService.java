package com.habittracker.service;

import com.habittracker.exception.GlobalExceptionHandler.NotFoundException;
import com.habittracker.model.Habit;
import com.habittracker.model.HabitCompletion;
import com.habittracker.repository.HabitCompletionRepository;
import com.habittracker.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CompletionService {

    private final HabitCompletionRepository completionRepository;
    private final HabitRepository habitRepository;
    private final StreakService streakService;
    private final AchievementService achievementService;

    @Transactional
    public Map<String, Object> complete(Long habitId, LocalDate date, Long userId) {
        Habit habit = habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new NotFoundException("Habit not found"));

        // Check if already completed
        if (completionRepository.findByHabitIdAndCompletedDate(habitId, date).isPresent()) {
            throw new IllegalArgumentException("Already completed for this date");
        }

        HabitCompletion completion = HabitCompletion.builder()
                .habit(habit)
                .completedDate(date)
                .build();
        completionRepository.save(completion);

        int currentStreak = streakService.calculateCurrentStreak(habitId);

        // Check for achievements
        achievementService.checkStreakAchievements(userId, habitId, currentStreak);

        // Check for perfect day
        boolean perfectDay = achievementService.checkPerfectDay(userId, date);

        return Map.of(
                "currentStreak", currentStreak,
                "perfectDay", perfectDay
        );
    }

    @Transactional
    public void uncomplete(Long habitId, LocalDate date, Long userId) {
        habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new NotFoundException("Habit not found"));

        HabitCompletion completion = completionRepository.findByHabitIdAndCompletedDate(habitId, date)
                .orElseThrow(() -> new NotFoundException("Completion not found"));

        completionRepository.delete(completion);
    }

    public List<LocalDate> getCompletions(Long habitId, LocalDate start, LocalDate end, Long userId) {
        habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new NotFoundException("Habit not found"));

        return completionRepository
                .findByHabitIdAndCompletedDateBetweenOrderByCompletedDateAsc(habitId, start, end)
                .stream()
                .map(HabitCompletion::getCompletedDate)
                .toList();
    }
}
