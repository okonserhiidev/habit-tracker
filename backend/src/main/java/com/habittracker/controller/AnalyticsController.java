package com.habittracker.controller;

import com.habittracker.dto.response.AchievementResponse;
import com.habittracker.dto.response.DashboardResponse;
import com.habittracker.dto.response.StatsResponse;
import com.habittracker.dto.response.TrendsResponse;
import com.habittracker.service.AchievementService;
import com.habittracker.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AchievementService achievementService;

    @GetMapping("/api/habits/{habitId}/stats")
    public StatsResponse getHabitStats(@PathVariable Long habitId, Authentication auth) {
        return analyticsService.getHabitStats(habitId, getUserId(auth));
    }

    @GetMapping("/api/habits/{habitId}/heatmap")
    public List<LocalDate> getHeatmap(@PathVariable Long habitId, Authentication auth) {
        return analyticsService.getHeatmapData(habitId, getUserId(auth));
    }

    @GetMapping("/api/achievements")
    public List<AchievementResponse> getAchievements(Authentication auth) {
        return achievementService.getUserAchievements(getUserId(auth)).stream()
                .map(AchievementResponse::from)
                .toList();
    }

    @GetMapping("/api/analytics/dashboard")
    public DashboardResponse getDashboard(Authentication auth) {
        return analyticsService.getDashboard(getUserId(auth));
    }

    @GetMapping("/api/analytics/trends")
    public TrendsResponse getTrends(Authentication auth) {
        return analyticsService.getTrends(getUserId(auth));
    }

    private Long getUserId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }
}
