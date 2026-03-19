package com.habittracker.repository;

import com.habittracker.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByUserIdOrderByUnlockedAtDesc(Long userId);
    Optional<Achievement> findByUserIdAndHabitIdAndType(Long userId, Long habitId, String type);
    Optional<Achievement> findByUserIdAndHabitIdIsNullAndType(Long userId, String type);
}
