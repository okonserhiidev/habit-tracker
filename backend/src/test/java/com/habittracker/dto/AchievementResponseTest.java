package com.habittracker.dto;

import com.habittracker.dto.response.AchievementResponse;
import com.habittracker.model.Achievement;
import com.habittracker.model.Habit;
import com.habittracker.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AchievementResponseTest {

    @Test
    void from_withHabit_mapsHabitId() {
        Achievement achievement = Achievement.builder()
                .id(1L)
                .user(User.builder().id(1L).build())
                .habit(Habit.builder().id(5L).build())
                .type("STREAK_7")
                .milestoneDays(7)
                .unlockedAt(LocalDateTime.of(2026, 3, 19, 10, 0))
                .build();

        AchievementResponse dto = AchievementResponse.from(achievement);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getType()).isEqualTo("STREAK_7");
        assertThat(dto.getMilestoneDays()).isEqualTo(7);
        assertThat(dto.getHabitId()).isEqualTo(5L);
        assertThat(dto.getUnlockedAt()).isEqualTo(LocalDateTime.of(2026, 3, 19, 10, 0));
    }

    @Test
    void from_withoutHabit_habitIdIsNull() {
        Achievement achievement = Achievement.builder()
                .id(2L)
                .user(User.builder().id(1L).build())
                .habit(null)
                .type("PERFECT_DAY")
                .build();

        AchievementResponse dto = AchievementResponse.from(achievement);

        assertThat(dto.getHabitId()).isNull();
        assertThat(dto.getType()).isEqualTo("PERFECT_DAY");
    }
}
