package com.habittracker.cucumber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.ru.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DashboardSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext ctx;

    private final ObjectMapper mapper = new ObjectMapper();
    private String resetToken;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    // Note: step "я отметил привычку X за сегодня" is defined in CompletionSteps

    @Когда("я запрашиваю dashboard")
    public void getDashboard() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/analytics/dashboard",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());
    }

    @Когда("я запрашиваю trends")
    public void getTrends() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/analytics/trends",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());
    }

    @Тогда("dashboard содержит activeHabits равное {int}")
    public void dashboardActiveHabits(int count) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.get("activeHabits").asInt()).isEqualTo(count);
    }

    @Тогда("dashboard содержит perfectDays больше {int}")
    public void dashboardPerfectDays(int min) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.get("perfectDays").asInt()).isGreaterThan(min);
    }

    @Тогда("trends содержит {int} дневных точек")
    public void trendsHasDailyPoints(int count) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.get("dailyTrend").size()).isEqualTo(count);
    }

    // === Password Reset ===

    @Когда("я запрашиваю сброс пароля для {string}")
    @Допустим("я запросил сброс пароля для {string}")
    public void requestPasswordReset(String email) throws Exception {
        String body = mapper.writeValueAsString(Map.of("email", email));

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/password/reset-request",
                HttpMethod.POST,
                new HttpEntity<>(body, jsonHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode json = mapper.readTree(response.getBody());
            if (json.has("token")) {
                resetToken = json.get("token").asText();
            }
        }
    }

    @Тогда("ответ содержит reset token")
    public void responseContainsResetToken() throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.has("token")).isTrue();
        assertThat(json.get("token").asText()).isNotEmpty();
    }

    @Когда("я сбрасываю пароль с полученным токеном на {string}")
    public void resetPassword(String newPassword) throws Exception {
        String body = mapper.writeValueAsString(Map.of("token", resetToken, "newPassword", newPassword));

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/password/reset",
                HttpMethod.POST,
                new HttpEntity<>(body, jsonHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());
    }

    @Тогда("я могу войти с email {string} и паролем {string}")
    public void canLoginWith(String email, String password) throws Exception {
        String body = mapper.writeValueAsString(Map.of("email", email, "password", password));

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(body, jsonHeaders()),
                String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        JsonNode json = mapper.readTree(response.getBody());
        assertThat(json.has("accessToken")).isTrue();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + ctx.getAccessToken());
        return headers;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
