package com.habittracker.controller;

import com.habittracker.dto.request.LoginRequest;
import com.habittracker.dto.request.RegisterRequest;
import com.habittracker.dto.response.AuthResponse;
import com.habittracker.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody Map<String, String> body) {
        return authService.refresh(body.get("refreshToken"));
    }

    @PostMapping("/password/reset-request")
    public Map<String, String> requestPasswordReset(@RequestBody Map<String, String> body) {
        String token = authService.requestPasswordReset(body.get("email"));
        return Map.of("message", "Password reset token generated", "token", token);
    }

    @PostMapping("/password/reset")
    public Map<String, String> resetPassword(@RequestBody Map<String, String> body) {
        authService.resetPassword(body.get("token"), body.get("newPassword"));
        return Map.of("message", "Password has been reset successfully");
    }
}
