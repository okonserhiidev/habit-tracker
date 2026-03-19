package com.habittracker.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    private static final String SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha-algorithms-to-work";
    private static final long ACCESS_EXPIRATION = 900_000L; // 15 min
    private static final long REFRESH_EXPIRATION = 2_592_000_000L; // 30 days

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(SECRET, ACCESS_EXPIRATION, REFRESH_EXPIRATION);
    }

    @Test
    void generateAccessToken_returnsNonEmptyString() {
        String token = tokenProvider.generateAccessToken(1L, "user@test.com");
        assertThat(token).isNotEmpty();
    }

    @Test
    void generateRefreshToken_returnsNonEmptyString() {
        String token = tokenProvider.generateRefreshToken(1L, "user@test.com");
        assertThat(token).isNotEmpty();
    }

    @Test
    void getUserIdFromToken_returnsCorrectUserId() {
        String token = tokenProvider.generateAccessToken(42L, "user@test.com");
        Long userId = tokenProvider.getUserIdFromToken(token);
        assertThat(userId).isEqualTo(42L);
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = tokenProvider.generateAccessToken(1L, "user@test.com");
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        assertThat(tokenProvider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    void validateToken_emptyToken_returnsFalse() {
        assertThat(tokenProvider.validateToken("")).isFalse();
    }

    @Test
    void validateToken_nullToken_returnsFalse() {
        assertThat(tokenProvider.validateToken(null)).isFalse();
    }

    @Test
    void isAccessToken_accessToken_returnsTrue() {
        String token = tokenProvider.generateAccessToken(1L, "user@test.com");
        assertThat(tokenProvider.isAccessToken(token)).isTrue();
    }

    @Test
    void isAccessToken_refreshToken_returnsFalse() {
        String token = tokenProvider.generateRefreshToken(1L, "user@test.com");
        assertThat(tokenProvider.isAccessToken(token)).isFalse();
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        JwtTokenProvider shortLived = new JwtTokenProvider(SECRET, -1000L, -1000L);
        String token = shortLived.generateAccessToken(1L, "user@test.com");
        assertThat(tokenProvider.validateToken(token)).isFalse();
    }

    @Test
    void differentUsers_produceDifferentTokens() {
        String token1 = tokenProvider.generateAccessToken(1L, "user1@test.com");
        String token2 = tokenProvider.generateAccessToken(2L, "user2@test.com");
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void accessAndRefreshTokens_areDifferent() {
        String access = tokenProvider.generateAccessToken(1L, "user@test.com");
        String refresh = tokenProvider.generateRefreshToken(1L, "user@test.com");
        assertThat(access).isNotEqualTo(refresh);
    }
}
