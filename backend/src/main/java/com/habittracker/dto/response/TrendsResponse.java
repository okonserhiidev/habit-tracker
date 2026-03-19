package com.habittracker.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TrendsResponse {
    private double currentWeekRate;   // % за текущую неделю
    private double previousWeekRate;  // % за прошлую неделю
    private double changePercent;     // разница: +3% или -2%
    private double currentMonthRate;
    private double previousMonthRate;
    private double monthChangePercent;
    private List<DailyPoint> dailyTrend; // тренд по дням за последние 30 дней

    @Data
    @Builder
    public static class DailyPoint {
        private String date;
        private int completed;
        private int total;
        private double rate;
    }
}
