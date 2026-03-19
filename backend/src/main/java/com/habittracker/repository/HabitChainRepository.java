package com.habittracker.repository;

import com.habittracker.model.HabitChain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HabitChainRepository extends JpaRepository<HabitChain, Long> {
    List<HabitChain> findByUserIdOrderByCreatedAtAsc(Long userId);
    Optional<HabitChain> findByIdAndUserId(Long id, Long userId);
}
