package com.habittracker.controller;

import com.habittracker.dto.request.FreezeDayRequest;
import com.habittracker.dto.response.FreezeDayResponse;
import com.habittracker.model.FreezeDay;
import com.habittracker.service.FreezeDayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FreezeDayController {

    private final FreezeDayService freezeDayService;

    @PostMapping("/api/habits/{habitId}/freeze")
    @ResponseStatus(HttpStatus.CREATED)
    public FreezeDayResponse freezeDay(
            @PathVariable Long habitId,
            @Valid @RequestBody FreezeDayRequest request,
            Authentication auth) {
        FreezeDay fd = freezeDayService.freezeDay(habitId, getUserId(auth), request.getDate());
        return FreezeDayResponse.from(fd);
    }

    @GetMapping("/api/freeze-days")
    public Map<String, Object> getFreezeDays(Authentication auth) {
        Long userId = getUserId(auth);
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<FreezeDayResponse> freezeDays = freezeDayService.getFreezeDays(userId, weekStart, weekEnd)
                .stream()
                .map(FreezeDayResponse::from)
                .toList();

        return Map.of(
                "freezeDays", freezeDays,
                "remainingThisWeek", freezeDayService.getRemainingFreezes(userId)
        );
    }

    private Long getUserId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }
}
