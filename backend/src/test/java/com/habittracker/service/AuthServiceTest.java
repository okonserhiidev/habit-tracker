package com.habittracker.service;

import com.habittracker.dto.request.LoginRequest;
import com.habittracker.dto.request.RegisterRequest;
import com.habittracker.dto.response.AuthResponse;
import com.habittracker.model.User;
import com.habittracker.repository.UserRepository;
import com.habittracker.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider tokenProvider;
    @InjectMocks private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .passwordHash("hashedPassword")
                .name("Test")
                .authProvider(User.AuthProvider.LOCAL)
                .build();
    }

    // === Register ===

    @Test
    void register_success_returnsAuthResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@test.com");
        request.setPassword("password123");
        request.setName("New User");

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(tokenProvider.generateAccessToken(anyLong(), anyString())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(anyLong(), anyString())).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser().getEmail()).isEqualTo("new@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@test.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already registered");

        verify(userRepository, never()).save(any());
    }

    // === Login ===

    @Test
    void login_success_returnsAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(tokenProvider.generateAccessToken(1L, "test@test.com")).thenReturn("access");
        when(tokenProvider.generateRefreshToken(1L, "test@test.com")).thenReturn("refresh");

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getUser().getId()).isEqualTo(1L);
    }

    @Test
    void login_wrongEmail_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("noone@test.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("noone@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_wrongPassword_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("wrongpass");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpass", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");
    }

    // === Refresh ===

    @Test
    void refresh_validToken_returnsNewTokens() {
        when(tokenProvider.validateToken("valid-refresh")).thenReturn(true);
        when(tokenProvider.getUserIdFromToken("valid-refresh")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateAccessToken(1L, "test@test.com")).thenReturn("new-access");
        when(tokenProvider.generateRefreshToken(1L, "test@test.com")).thenReturn("new-refresh");

        AuthResponse response = authService.refresh("valid-refresh");

        assertThat(response.getAccessToken()).isEqualTo("new-access");
    }

    @Test
    void refresh_invalidToken_throwsException() {
        when(tokenProvider.validateToken("bad-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh("bad-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid refresh token");
    }

    // === Password Reset ===

    @Test
    void requestPasswordReset_success_returnsToken() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        String token = authService.requestPasswordReset("test@test.com");

        assertThat(token).isNotNull().isNotEmpty();
        verify(userRepository).save(argThat(u -> u.getResetToken() != null && u.getResetTokenExpiresAt() != null));
    }

    @Test
    void requestPasswordReset_emailNotFound_throwsException() {
        when(userRepository.findByEmail("noone@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.requestPasswordReset("noone@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email not found");
    }

    @Test
    void requestPasswordReset_oauthUser_throwsException() {
        User googleUser = User.builder()
                .id(2L).email("google@test.com").authProvider(User.AuthProvider.GOOGLE).build();
        when(userRepository.findByEmail("google@test.com")).thenReturn(Optional.of(googleUser));

        assertThatThrownBy(() -> authService.requestPasswordReset("google@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password reset is not available for OAuth accounts");
    }

    @Test
    void resetPassword_success_updatesPassword() {
        testUser.setResetToken("valid-token");
        testUser.setResetTokenExpiresAt(LocalDateTime.now().plusHours(1));
        when(userRepository.findByResetToken("valid-token")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpass123")).thenReturn("newHashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.resetPassword("valid-token", "newpass123");

        verify(userRepository).save(argThat(u ->
                u.getPasswordHash().equals("newHashed") &&
                u.getResetToken() == null &&
                u.getResetTokenExpiresAt() == null
        ));
    }

    @Test
    void resetPassword_invalidToken_throwsException() {
        when(userRepository.findByResetToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword("bad-token", "newpass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid reset token");
    }

    @Test
    void resetPassword_expiredToken_throwsException() {
        testUser.setResetToken("expired-token");
        testUser.setResetTokenExpiresAt(LocalDateTime.now().minusHours(1));
        when(userRepository.findByResetToken("expired-token")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.resetPassword("expired-token", "newpass123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reset token has expired");
    }

    @Test
    void resetPassword_shortPassword_throwsException() {
        testUser.setResetToken("token");
        testUser.setResetTokenExpiresAt(LocalDateTime.now().plusHours(1));
        when(userRepository.findByResetToken("token")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.resetPassword("token", "123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 6 characters");
    }
}
