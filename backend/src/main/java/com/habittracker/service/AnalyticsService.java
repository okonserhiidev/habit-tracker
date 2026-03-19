package com.habittracker.service;

import com.habittracker.dto.response.DashboardResponse;
import com.habittracker.dto.response.StatsResponse;
import com.habittracker.dto.response.TrendsResponse;
import com.habittracker.model.Habit;
import com.habittracker.model.HabitCompletion;
import com.habittracker.repository.HabitCompletionRepository;
import com.habittracker.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final HabitCompletionRepository completionRepository;
    private final HabitRepository habitRepository;
    private final StreakService streakService;

    public StatsResponse getHabitStats(Long habitId, Long userId) {
        habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Habit not found"));

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusWeeks(1);
        LocalDate monthAgo = today.minusMonths(1);

        List<HabitCompletion> weekCompletions =
                completionRepository.findByHabitIdAndCompletedDateBetweenOrderByCompletedDateAsc(habitId, weekAgo, today);
        List<HabitCompletion> monthCompletions =
                completionRepository.findByHabitIdAndCompletedDateBetweenOrderByCompletedDateAsc(habitId, monthAgo, today);

        // Completions by day of week
        Map<String, Long> byDayOfWeek = monthCompletions.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCompletedDate().getDayOfWeek().name(),
                        Collectors.counting()));

        // Ensure all days present
        Map<String, Long> orderedByDay = new LinkedHashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            orderedByDay.put(day.name(), byDayOfWeek.getOrDefault(day.name(), 0L));
        }

        return StatsResponse.builder()
                .currentStreak(streakService.calculateCurrentStreak(habitId))
                .bestStreak(streakService.calculateBestStreak(habitId))
                .totalCompletions(completionRepository.countByHabitId(habitId))
                .completionRateWeek((double) weekCompletions.size() / 7 * 100)
                .completionRateMonth((double) monthCompletions.size() / 30 * 100)
                .completionsByDayOfWeek(orderedByDay)
                .build();
    }

    public List<LocalDate> getHeatmapData(Long habitId, Long userId) {
        habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Habit not found"));

        LocalDate yearAgo = LocalDate.now().minusYears(1);
        return completionRepository
                .findByHabitIdAndCompletedDateBetweenOrderByCompletedDateAsc(habitId, yearAgo, LocalDate.now())
                .stream()
                .map(HabitCompletion::getCompletedDate)
                .toList();
    }

    // ==================== Dashboard ====================

    public DashboardResponse getDashboard(Long userId) {
        List<Habit> activeHabits = habitRepository.findByUserIdAndIsArchivedFalseOrderByPositionAsc(userId);
        if (activeHabits.isEmpty()) {
            return DashboardResponse.builder()
                    .activeHabits(0)
                    .overallCompletionRate(0)
                    .perfectDays(0)
                    .perfectDayStreak(0)
                    .topHabits(List.of())
                    .needAttention(List.of())
                    .completionByDayOfWeek(new LinkedHashMap<>())
                    .build();
        }

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusWeeks(1);
        LocalDate monthAgo = today.minusMonths(1);
        int totalHabits = activeHabits.size();

        // Per-habit stats
        List<DashboardResponse.HabitSummary> summaries = new ArrayList<>();
        for (Habit h : activeHabits) {
            long weekCompletions = completionRepository
                    .findByHabitIdAndCompletedDateBetweenOrderByCompletedDateAsc(h.getId(), weekAgo, today)
                    .size();
            double rate = (double) weekCompletions / 7 * 100;
            summaries.add(DashboardResponse.HabitSummary.builder()
                    .id(h.getId())
                    .name(h.getName())
                    .color(h.getColor())
                    .completionRate(Math.round(rate * 10) / 10.0)
                    .currentStreak(streakService.calculateCurrentStreak(h.getId()))
                    .build());
        }

        // Overall week rate
        double avgRate = summaries.stream()
                .mapToDouble(DashboardResponse.HabitSummary::getCompletionRate)
                .average().orElse(0);

        // Top habits (>= 50%, sorted desc)
        List<DashboardResponse.HabitSummary> top = summaries.stream()
                .filter(s -> s.getCompletionRate() >= 50)
                .sorted(Comparator.comparingDouble(DashboardResponse.HabitSummary::getCompletionRate).reversed())
                .toList();

        // Need attention (< 50%)
        List<DashboardResponse.HabitSummary> attention = summaries.stream()
                .filter(s -> s.getCompletionRate() < 50)
                .sorted(Comparator.comparingDouble(DashboardResponse.HabitSummary::getCompletionRate))
                .toList();

        // Perfect days last month
        int perfectDays = 0;
        int perfectDayStreak = 0;
        int currentPerfectStreak = 0;
        for (LocalDate d = today; !d.isBefore(monthAgo); d = d.minusDays(1)) {
            List<HabitCompletion> dayCompletions = completionRepository.findByUserIdAndDate(userId, d);
            if (dayCompletions.size() >= totalHabits) {
                perfectDays++;
                currentPerfectStreak++;
                perfectDayStreak = Math.max(perfectDayStreak, currentPerfectStreak);
            } else {
                currentPerfectStreak = 0;
            }
        }

        // Completion by day of week (average across all habits, last month)
        Map<String, Double> byDay = new LinkedHashMap<>();
        for (DayOfWeek dow : DayOfWeek.values()) {
            long totalForDay = 0;
            long possibleForDay = 0;
            for (Habit h : activeHabits) {
                List<HabitCompletion> monthComps = completionRepository
                        .findByHabitIdAndCompletedDateBetweenOrderByCompletedDateAsc(h.getId(), monthAgo, today);
                totalForDay += monthComps.stream()
                        .filter(c -> c.getCompletedDate().getDayOfWeek() == dow)
                        .count();
                // Count how many of this day of week exist in the range
                for (LocalDate d = monthAgo; !d.isAfter(today); d = d.plusDays(1)) {
                    if (d.getDayOfWeek() == dow) possibleForDay++;
                }
            }
            // possibleForDay already multiplied by number of habits due to loop
            byDay.put(dow.name(), possibleForDay > 0 ? Math.round((double) totalForDay / possibleForDay * 1000) / 10.0 : 0);
        }

        return DashboardResponse.builder()
                .activeHabits(totalHabits)
                .overallCompletionRate(Math.round(avgRate * 10) / 10.0)
                .perfectDays(perfectDays)
                .perfectDayStreak(perfectDayStreak)
                .topHabits(top)
                .needAttention(attention)
                .completionByDayOfWeek(byDay)
                .build();
    }

    // ==================== Trends ====================

    public TrendsResponse getTrends(Long userId) {
        List<Habit> activeHabits = habitRepository.findByUserIdAndIsArchivedFalseOrderByPositionAsc(userId);
        LocalDate today = LocalDate.now();
        int totalHabits = activeHabits.size();

        if (totalHabits == 0) {
            return TrendsResponse.builder()
                    .currentWeekRate(0).previousWeekRate(0).changePercent(0)
                    .currentMonthRate(0).previousMonthRate(0).monthChangePercent(0)
                    .dailyTrend(List.of())
                    .build();
        }

        // Week comparison
        double currentWeekRate = calcPeriodRate(activeHabits, today.minusDays(6), today);
        double previousWeekRate = calcPeriodRate(activeHabits, today.minusDays(13), today.minusDays(7));

        // Month comparison
        double currentMonthRate = calcPeriodRate(activeHabits, today.minusDays(29), today);
        double previousMonthRate = calcPeriodRate(activeHabits, today.minusDays(59), today.minusDays(30));

        // Daily trend (last 30 days)
        List<TrendsResponse.DailyPoint> dailyTrend = new ArrayList<>();
        for (LocalDate d = today.minusDays(29); !d.isAfter(today); d = d.plusDays(1)) {
            List<HabitCompletion> dayComps = completionRepository.findByUserIdAndDate(userId, d);
            int completed = dayComps.size();
            double rate = totalHabits > 0 ? (double) completed / totalHabits * 100 : 0;
            dailyTrend.add(TrendsResponse.DailyPoint.builder()
                    .date(d.toString())
                    .completed(completed)
                    .total(totalHabits)
                    .rate(Math.round(rate * 10) / 10.0)
                    .build());
        }

        return TrendsResponse.builder()
                .currentWeekRate(Math.round(currentWeekRate * 10) / 10.0)
                .previousWeekRate(Math.round(previousWeekRate * 10) / 10.0)
                .changePercent(Math.round((currentWeekRate - previousWeekRate) * 10) / 10.0)
                .currentMonthRate(Math.round(currentMonthRate * 10) / 10.0)
                .previousMonthRate(Math.round(previousMonthRate * 10) / 10.0)
                .monthChangePercent(Math.round((currentMonthRate - previousMonthRate) * 10) / 10.0)
                .dailyTrend(dailyTrend)
                .build();
    }

    private double calcPeriodRate(List<Habit> habits, LocalDate from, LocalDate to) {
        long totalPossible = 0;
        long totalCompleted = 0;
        long days = from.until(to).getDays() + 1;

        for (Habit h : habits) {
            totalPossible += days;
            totalCompleted += completionRepository
                    .findByHabitIdAndCompletedDateBetweenOrderByCompletedDateAsc(h.getId(), from, to)
                    .size();
        }

        return totalPossible > 0 ? (double) totalCompleted / totalPossible * 100 : 0;
    }
}
