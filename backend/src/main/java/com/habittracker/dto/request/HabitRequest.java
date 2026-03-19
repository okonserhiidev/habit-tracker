package com.habittracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class HabitRequest {
    @NotBlank
    private String name;

    private String description;

    @NotBlank
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex code like #FF5733")
    private String color;

    private String icon;
    private String category;
    private String identityText;
    private String miniVersion;
    private String frequency; // DAILY, WEEKDAYS, CUSTOM
    private String customDays; // "MON,WED,FRI"
    private String reminderTime; // "09:00"
}
