package com.habittracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_id")
    private Habit habit; // nullable — общие достижения

    @Column(nullable = false)
    private String type; // STREAK_7, STREAK_21, STREAK_30, STREAK_66, STREAK_100, PERFECT_DAY

    private Integer milestoneDays;

    private LocalDateTime unlockedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (unlockedAt == null) unlockedAt = LocalDateTime.now();
    }
}
