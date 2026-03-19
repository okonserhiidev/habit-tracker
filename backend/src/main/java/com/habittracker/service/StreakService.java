package com.habittracker.service;

import com.habittracker.model.FreezeDay;
import com.habittracker.repository.FreezeDayRepository;
import com.habittracker.repository.HabitCompletionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final HabitCompletionRepository completionRepository;
    private final FreezeDayRepository freezeDayRepository;

    public int calculateCurrentStreak(Long habitId) {
        List<LocalDate> completedDates = completionRepository.findCompletedDatesByHabitIdDesc(habitId);
        Set<LocalDate> completedSet = Set.copyOf(completedDates);

        Set<LocalDate> frozenDates = freezeDayRepository.findByHabitId(habitId).stream()
                .map(FreezeDay::getFreezeDate)
                .collect(Collectors.toSet());

        int streak = 0;
        LocalDate date = LocalDate.now();

        // If today is not completed and not frozen, start checking from yesterday
        if (!completedSet.contains(date) && !frozenDates.contains(date)) {
            date = date.minusDays(1);
        }

        while (completedSet.contains(date) || frozenDates.contains(date)) {
            streak++;
            date = date.minusDays(1);
        }

        return streak;
    }

    public int calculateBestStreak(Long habitId) {
        List<LocalDate> completedDates = completionRepository.findCompletedDatesByHabitIdDesc(habitId);
        if (completedDates.isEmpty()) return 0;

        Set<LocalDate> frozenDates = freezeDayRepository.findByHabitId(habitId).stream()
                .map(FreezeDay::getFreezeDate)
                .collect(Collectors.toSet());

        // Sort ascending for best streak calculation
        List<LocalDate> sorted = completedDates.stream().sorted().toList();

        int bestStreak = 1;
        int currentStreak = 1;

        for (int i = 1; i < sorted.size(); i++) {
            LocalDate prev = sorted.get(i - 1);
            LocalDate curr = sorted.get(i);

            // Count frozen days between prev and curr
            long daysBetween = curr.toEpochDay() - prev.toEpochDay();
            boolean allFrozenBetween = true;

            if (daysBetween > 1) {
                for (long d = 1; d < daysBetween; d++) {
                    if (!frozenDates.contains(prev.plusDays(d))) {
                        allFrozenBetween = false;
                        break;
                    }
                }
            }

            if (daysBetween == 1 || allFrozenBetween) {
                currentStreak++;
            } else {
                currentStreak = 1;
            }

            bestStreak = Math.max(bestStreak, currentStreak);
        }

        return bestStreak;
    }
}
