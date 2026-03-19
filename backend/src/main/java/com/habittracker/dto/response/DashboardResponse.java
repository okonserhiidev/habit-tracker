package com.habittracker.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardResponse {
    private int activeHabits;
    private double overallCompletionRate;  // % за текущую неделю
    private int perfectDays;               // дней с полным выполнением за месяц
    private int perfectDayStreak;          // серия идеальных дней подряд
    private List<HabitSummary> topHabits;  // топ по стабильности
    private List<HabitSummary> needAttention; // проседающие привычки
    private Map<String, Double> completionByDayOfWeek; // средний % по дням недели

    @Data
    @Builder
    public static class HabitSummary {
        private Long id;
        private String name;
        private String color;
        private double completionRate;
        private int currentStreak;
    }
}
