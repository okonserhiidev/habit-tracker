package com.habittracker.controller;

import com.habittracker.dto.request.HabitRequest;
import com.habittracker.dto.response.HabitResponse;
import com.habittracker.service.HabitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;

    @GetMapping
    public List<HabitResponse> getHabits(Authentication auth) {
        return habitService.getHabits(getUserId(auth));
    }

    @GetMapping("/{id}")
    public HabitResponse getHabit(@PathVariable Long id, Authentication auth) {
        return habitService.getHabit(id, getUserId(auth));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HabitResponse createHabit(@Valid @RequestBody HabitRequest request, Authentication auth) {
        return habitService.createHabit(request, getUserId(auth));
    }

    @PutMapping("/{id}")
    public HabitResponse updateHabit(@PathVariable Long id,
                                     @Valid @RequestBody HabitRequest request,
                                     Authentication auth) {
        return habitService.updateHabit(id, request, getUserId(auth));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHabit(@PathVariable Long id, Authentication auth) {
        habitService.deleteHabit(id, getUserId(auth));
    }

    @PatchMapping("/{id}/archive")
    public HabitResponse archiveHabit(@PathVariable Long id, Authentication auth) {
        return habitService.archiveHabit(id, getUserId(auth));
    }

    @PutMapping("/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderHabits(@RequestBody List<Long> habitIds, Authentication auth) {
        habitService.reorderHabits(habitIds, getUserId(auth));
    }

    private Long getUserId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }
}
