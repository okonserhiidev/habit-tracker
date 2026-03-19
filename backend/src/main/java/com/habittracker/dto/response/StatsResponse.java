package com.habittracker.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class StatsResponse {
    private Integer currentStreak;
    private Integer bestStreak;
    private Long totalCompletions;
    private Double completionRateWeek;
    private Double completionRateMonth;
    private Map<String, Long> completionsByDayOfWeek; // MON: 12, TUE: 8, ...
}
