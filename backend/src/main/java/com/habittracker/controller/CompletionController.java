package com.habittracker.controller;

import com.habittracker.dto.request.CompletionRequest;
import com.habittracker.service.CompletionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/habits/{habitId}")
@RequiredArgsConstructor
public class CompletionController {

    private final CompletionService completionService;

    @PostMapping("/complete")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> complete(@PathVariable Long habitId,
                                        @Valid @RequestBody CompletionRequest request,
                                        Authentication auth) {
        return completionService.complete(habitId, request.getDate(), getUserId(auth));
    }

    @DeleteMapping("/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uncomplete(@PathVariable Long habitId,
                           @Valid @RequestBody CompletionRequest request,
                           Authentication auth) {
        completionService.uncomplete(habitId, request.getDate(), getUserId(auth));
    }

    @GetMapping("/completions")
    public List<LocalDate> getCompletions(@PathVariable Long habitId,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                                          Authentication auth) {
        return completionService.getCompletions(habitId, start, end, getUserId(auth));
    }

    private Long getUserId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }
}
