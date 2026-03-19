package com.habittracker.dto.response;

import com.habittracker.model.Achievement;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AchievementResponse {
    private Long id;
    private String type;
    private Integer milestoneDays;
    private Long habitId;
    private LocalDateTime unlockedAt;

    public static AchievementResponse from(Achievement a) {
        AchievementResponse dto = new AchievementResponse();
        dto.setId(a.getId());
        dto.setType(a.getType());
        dto.setMilestoneDays(a.getMilestoneDays());
        dto.setHabitId(a.getHabit() != null ? a.getHabit().getId() : null);
        dto.setUnlockedAt(a.getUnlockedAt());
        return dto;
    }
}
