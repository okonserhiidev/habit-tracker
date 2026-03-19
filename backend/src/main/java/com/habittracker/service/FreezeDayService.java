package com.habittracker.service;

import com.habittracker.model.FreezeDay;
import com.habittracker.model.Habit;
import com.habittracker.model.User;
import com.habittracker.repository.FreezeDayRepository;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FreezeDayService {

    private static final int MAX_FREEZE_PER_WEEK = 2;

    private final FreezeDayRepository freezeDayRepository;
    private final HabitRepository habitRepository;
    private final UserRepository userRepository;

    @Transactional
    public FreezeDay freezeDay(Long habitId, Long userId, LocalDate date) {
        Habit habit = habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Habit not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot freeze a future date");
        }

        // Check weekly limit (Monday to Sunday of the date's week)
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        long usedThisWeek = freezeDayRepository.countByUserIdAndFreezeDateBetween(userId, weekStart, weekEnd);

        if (usedThisWeek >= MAX_FREEZE_PER_WEEK) {
            throw new IllegalArgumentException(
                    "Freeze day limit reached (" + MAX_FREEZE_PER_WEEK + " per week)");
        }

        // Check duplicate
        List<FreezeDay> existing = freezeDayRepository.findByUserIdAndFreezeDateBetween(userId, date, date);
        boolean alreadyFrozen = existing.stream()
                .anyMatch(fd -> fd.getHabit().getId().equals(habitId));
        if (alreadyFrozen) {
            throw new IllegalArgumentException("This day is already frozen for this habit");
        }

        FreezeDay freezeDay = FreezeDay.builder()
                .user(user)
                .habit(habit)
                .freezeDate(date)
                .build();

        return freezeDayRepository.save(freezeDay);
    }

    public List<FreezeDay> getFreezeDays(Long userId, LocalDate from, LocalDate to) {
        return freezeDayRepository.findByUserIdAndFreezeDateBetween(userId, from, to);
    }

    public int getRemainingFreezes(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        long used = freezeDayRepository.countByUserIdAndFreezeDateBetween(userId, weekStart, weekEnd);
        return (int) Math.max(0, MAX_FREEZE_PER_WEEK - used);
    }
}
