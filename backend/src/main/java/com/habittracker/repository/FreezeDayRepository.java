package com.habittracker.repository;

import com.habittracker.model.FreezeDay;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface FreezeDayRepository extends JpaRepository<FreezeDay, Long> {
    List<FreezeDay> findByHabitId(Long habitId);
    List<FreezeDay> findByUserIdAndFreezeDateBetween(Long userId, LocalDate start, LocalDate end);
    long countByUserIdAndFreezeDateBetween(Long userId, LocalDate start, LocalDate end);
}
