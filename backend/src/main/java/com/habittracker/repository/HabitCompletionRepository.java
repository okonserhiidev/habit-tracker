package com.habittracker.repository;

import com.habittracker.model.HabitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {

    Optional<HabitCompletion> findByHabitIdAndCompletedDate(Long habitId, LocalDate date);

    List<HabitCompletion> findByHabitIdAndCompletedDateBetweenOrderByCompletedDateAsc(
            Long habitId, LocalDate start, LocalDate end);

    List<HabitCompletion> findByHabitIdOrderByCompletedDateDesc(Long habitId);

    long countByHabitId(Long habitId);

    @Query("SELECT hc.completedDate FROM HabitCompletion hc WHERE hc.habit.id = :habitId ORDER BY hc.completedDate DESC")
    List<LocalDate> findCompletedDatesByHabitIdDesc(@Param("habitId") Long habitId);

    @Query("SELECT hc FROM HabitCompletion hc WHERE hc.habit.user.id = :userId AND hc.completedDate = :date")
    List<HabitCompletion> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
