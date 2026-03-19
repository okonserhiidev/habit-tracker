package com.habittracker.dto;

import com.habittracker.dto.response.UserResponse;
import com.habittracker.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseTest {

    @Test
    void from_mapsAllFields() {
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("Test User")
                .avatarUrl("https://avatar.url")
                .build();

        UserResponse dto = UserResponse.from(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getEmail()).isEqualTo("test@test.com");
        assertThat(dto.getName()).isEqualTo("Test User");
        assertThat(dto.getAvatarUrl()).isEqualTo("https://avatar.url");
    }

    @Test
    void from_nullOptionalFields_mapsToNull() {
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        UserResponse dto = UserResponse.from(user);

        assertThat(dto.getName()).isNull();
        assertThat(dto.getAvatarUrl()).isNull();
    }
}
