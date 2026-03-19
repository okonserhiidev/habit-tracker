package com.habittracker.cucumber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.ru.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

public class AnalyticsSteps {

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

    @Когда("я запрашиваю статистику последней привычки")
    public void getLastHabitStats() {
        getStats(ctx.getLastHabitId());
    }

    @Когда("я запрашиваю статистику привычки {string}")
    public void getNamedHabitStats(String name) {
        Long habitId = ctx.getHabitNameToId().get(name);
        assertThat(habitId).as("Habit '%s' not found in context", name).isNotNull();
        getStats(habitId);
    }

    @Когда("я запрашиваю heatmap последней привычки")
    public void getHeatmap() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/habits/" + ctx.getLastHabitId() + "/heatmap",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());
    }

    @Когда("я запрашиваю достижения")
    public void getAchievements() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/achievements",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());
    }

    // ===== Assertions =====

    @Тогда("лучший streak равен {int}")
    public void bestStreakEquals(int expected) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.get("bestStreak").asInt()).isEqualTo(expected);
    }

    @Тогда("всего выполнений {int}")
    public void totalCompletionsEquals(int expected) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.get("totalCompletions").asInt()).isEqualTo(expected);
    }

    @Тогда("процент за неделю больше {int}")
    public void weekRateGreaterThan(int value) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.get("completionRateWeek").asDouble()).isGreaterThan(value);
    }

    @Тогда("ответ содержит статистику по дням недели")
    public void responseHasDayOfWeekStats() throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.has("completionsByDayOfWeek")).isTrue();
        JsonNode days = json.get("completionsByDayOfWeek");
        assertThat(days.has("MONDAY")).isTrue();
        assertThat(days.has("SUNDAY")).isTrue();
    }

    @Тогда("heatmap содержит минимум {int} дату")
    public void heatmapHasMinDates(int count) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.size()).isGreaterThanOrEqualTo(count);
    }

    private void getStats(Long habitId) {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/habits/" + habitId + "/stats",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + ctx.getAccessToken());
        return headers;
    }
}
