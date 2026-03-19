package com.habittracker.cucumber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.ru.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext ctx;

    private final ObjectMapper mapper = new ObjectMapper();

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Когда("я отправляю запрос на регистрацию с email {string} и паролем {string}")
    public void registerRequest(String email, String password) throws Exception {
        String body = mapper.writeValueAsString(
                java.util.Map.of("email", email, "password", password, "name", "Test"));

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/register",
                HttpMethod.POST,
                new HttpEntity<>(body, jsonHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode json = mapper.readTree(response.getBody());
            if (json.has("accessToken")) {
                ctx.setAccessToken(json.get("accessToken").asText());
                ctx.setRefreshToken(json.get("refreshToken").asText());
            }
        }
    }

    @Допустим("пользователь с email {string} уже зарегистрирован")
    public void userAlreadyRegistered(String email) throws Exception {
        registerRequest(email, "password123");
    }

    @Допустим("пользователь с email {string} и паролем {string} зарегистрирован")
    public void userRegisteredWithPassword(String email, String password) throws Exception {
        registerRequest(email, password);
    }

    @Когда("я отправляю запрос на вход с email {string} и паролем {string}")
    public void loginRequest(String email, String password) throws Exception {
        String body = mapper.writeValueAsString(
                java.util.Map.of("email", email, "password", password));

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(body, jsonHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode json = mapper.readTree(response.getBody());
            if (json.has("accessToken")) {
                ctx.setAccessToken(json.get("accessToken").asText());
                ctx.setRefreshToken(json.get("refreshToken").asText());
            }
        }
    }

    @Допустим("я вошел как {string} с паролем {string}")
    public void loggedIn(String email, String password) throws Exception {
        loginRequest(email, password);
    }

    @Когда("я отправляю запрос на обновление токена")
    public void refreshTokenRequest() throws Exception {
        String body = mapper.writeValueAsString(
                java.util.Map.of("refreshToken", ctx.getRefreshToken()));

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/refresh",
                HttpMethod.POST,
                new HttpEntity<>(body, jsonHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode json = mapper.readTree(response.getBody());
            if (json.has("accessToken")) {
                ctx.setAccessToken(json.get("accessToken").asText());
            }
        }
    }

    // ===== Assertions =====

    @Тогда("я получаю ответ со статусом {int}")
    public void checkStatus(int status) {
        assertThat(ctx.getLastStatusCode())
                .as("Expected status %d but got %d. Response body: %s",
                        status, ctx.getLastStatusCode(), ctx.getLastResponseBody())
                .isEqualTo(status);
    }

    @Тогда("ответ содержит accessToken")
    public void responseContainsAccessToken() throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.has("accessToken")).isTrue();
        assertThat(json.get("accessToken").asText()).isNotEmpty();
    }

    @Тогда("ответ содержит refreshToken")
    public void responseContainsRefreshToken() throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.has("refreshToken")).isTrue();
    }

    @Тогда("ответ содержит пользователя с email {string}")
    public void responseContainsUserWithEmail(String email) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.path("user").path("email").asText()).isEqualTo(email);
    }

    @Тогда("ответ содержит ошибку {string}")
    public void responseContainsError(String errorMessage) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.path("error").asText()).isEqualTo(errorMessage);
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
