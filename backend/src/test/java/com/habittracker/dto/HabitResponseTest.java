package com.habittracker.dto;

import com.habittracker.dto.response.HabitResponse;
import com.habittracker.model.Habit;
import com.habittracker.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class HabitResponseTest {

    @Test
    void from_mapsAllFields() {
        Habit habit = Habit.builder()
                .id(1L)
                .user(User.builder().id(1L).build())
                .name("Чтение")
                .description("30 минут")
                .color("#FF5733")
                .icon("book")
                .category("Учёба")
                .identityText("Я — читающий")
                .miniVersion("1 страница")
                .frequency("DAILY")
                .customDays("MON,WED,FRI")
                .reminderTime(LocalTime.of(9, 0))
                .position(2)
                .isArchived(false)
                .build();

        HabitResponse dto = HabitResponse.from(habit);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Чтение");
        assertThat(dto.getDescription()).isEqualTo("30 минут");
        assertThat(dto.getColor()).isEqualTo("#FF5733");
        assertThat(dto.getIcon()).isEqualTo("book");
        assertThat(dto.getCategory()).isEqualTo("Учёба");
        assertThat(dto.getIdentityText()).isEqualTo("Я — читающий");
        assertThat(dto.getMiniVersion()).isEqualTo("1 страница");
        assertThat(dto.getFrequency()).isEqualTo("DAILY");
        assertThat(dto.getCustomDays()).isEqualTo("MON,WED,FRI");
        assertThat(dto.getReminderTime()).isEqualTo("09:00");
        assertThat(dto.getPosition()).isEqualTo(2);
        assertThat(dto.getIsArchived()).isFalse();
    }

    @Test
    void from_nullReminderTime_mapsToNull() {
        Habit habit = Habit.builder()
                .id(1L)
                .user(User.builder().id(1L).build())
                .name("Тест")
                .color("#000000")
                .frequency("DAILY")
                .position(0)
                .isArchived(false)
                .build();

        HabitResponse dto = HabitResponse.from(habit);

        assertThat(dto.getReminderTime()).isNull();
    }

    @Test
    void from_completedTodayAndStreak_notSetByMapper() {
        Habit habit = Habit.builder()
                .id(1L)
                .user(User.builder().id(1L).build())
                .name("Тест")
                .color("#000000")
                .frequency("DAILY")
                .position(0)
                .isArchived(false)
                .build();

        HabitResponse dto = HabitResponse.from(habit);

        // These are set by the service, not by the mapper
        assertThat(dto.getCompletedToday()).isNull();
        assertThat(dto.getCurrentStreak()).isNull();
    }
}
