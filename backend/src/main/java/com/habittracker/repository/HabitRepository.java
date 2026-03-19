package com.habittracker.repository;

import com.habittracker.model.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findByUserIdAndIsArchivedFalseOrderByPositionAsc(Long userId);
    List<Habit> findByUserIdOrderByPositionAsc(Long userId);
    Optional<Habit> findByIdAndUserId(Long id, Long userId);
    int countByUserIdAndIsArchivedFalse(Long userId);
}
