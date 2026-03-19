package com.habittracker.dto.response;

import com.habittracker.model.Habit;
import lombok.Data;

@Data
public class HabitResponse {
    private Long id;
    private String name;
    private String description;
    private String color;
    private String icon;
    private String category;
    private String identityText;
    private String miniVersion;
    private String frequency;
    private String customDays;
    private String reminderTime;
    private Integer position;
    private Boolean isArchived;
    private Boolean completedToday;
    private Integer currentStreak;

    public static HabitResponse from(Habit habit) {
        HabitResponse dto = new HabitResponse();
        dto.setId(habit.getId());
        dto.setName(habit.getName());
        dto.setDescription(habit.getDescription());
        dto.setColor(habit.getColor());
        dto.setIcon(habit.getIcon());
        dto.setCategory(habit.getCategory());
        dto.setIdentityText(habit.getIdentityText());
        dto.setMiniVersion(habit.getMiniVersion());
        dto.setFrequency(habit.getFrequency());
        dto.setCustomDays(habit.getCustomDays());
        dto.setReminderTime(habit.getReminderTime() != null ? habit.getReminderTime().toString() : null);
        dto.setPosition(habit.getPosition());
        dto.setIsArchived(habit.getIsArchived());
        return dto;
    }
}
