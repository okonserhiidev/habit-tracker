package com.habittracker.service;

import com.habittracker.model.Achievement;
import com.habittracker.model.Habit;
import com.habittracker.repository.AchievementRepository;
import com.habittracker.repository.HabitCompletionRepository;
import com.habittracker.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private static final int[] STREAK_MILESTONES = {7, 21, 30, 66, 100, 365};

    private final AchievementRepository achievementRepository;
    private final HabitCompletionRepository completionRepository;
    private final HabitRepository habitRepository;

    public List<Achievement> getUserAchievements(Long userId) {
        return achievementRepository.findByUserIdOrderByUnlockedAtDesc(userId);
    }

    public void checkStreakAchievements(Long userId, Long habitId, int currentStreak) {
        for (int milestone : STREAK_MILESTONES) {
            if (currentStreak >= milestone) {
                String type = "STREAK_" + milestone;
                if (achievementRepository.findByUserIdAndHabitIdAndType(userId, habitId, type).isEmpty()) {
                    Achievement achievement = Achievement.builder()
                            .user(com.habittracker.model.User.builder().id(userId).build())
                            .habit(Habit.builder().id(habitId).build())
                            .type(type)
                            .milestoneDays(milestone)
                            .build();
                    achievementRepository.save(achievement);
                }
            }
        }
    }

    public boolean checkPerfectDay(Long userId, LocalDate date) {
        List<Habit> activeHabits = habitRepository.findByUserIdAndIsArchivedFalseOrderByPositionAsc(userId);
        if (activeHabits.isEmpty()) return false;

        long completedCount = completionRepository.findByUserIdAndDate(userId, date).size();
        boolean isPerfect = completedCount >= activeHabits.size();

        if (isPerfect) {
            String type = "PERFECT_DAY";
            if (achievementRepository.findByUserIdAndHabitIdIsNullAndType(userId, type).isEmpty()) {
                Achievement achievement = Achievement.builder()
                        .user(com.habittracker.model.User.builder().id(userId).build())
                        .type(type)
                        .build();
                achievementRepository.save(achievement);
            }
        }

        return isPerfect;
    }
}
