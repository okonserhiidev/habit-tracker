package com.habittracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "habits")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Habit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, length = 7)
    private String color; // hex: #FF5733

    private String icon;

    private String category;

    private String identityText; // "Кем я хочу стать?"

    private String miniVersion; // Правило двух минут

    @Column(nullable = false)
    private String frequency; // DAILY, WEEKDAYS, CUSTOM

    private String customDays; // "MON,WED,FRI"

    private LocalTime reminderTime;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false)
    private Boolean isArchived;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isArchived == null) isArchived = false;
        if (position == null) position = 0;
        if (frequency == null) frequency = "DAILY";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
