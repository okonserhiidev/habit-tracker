package com.habittracker.cucumber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.ru.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CompletionSteps {

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

    @Когда("я отмечаю последнюю привычку как выполненную за сегодня")
    public void completeToday() throws Exception {
        completeForDate(ctx.getLastHabitId(), LocalDate.now().toString());
    }

    @Допустим("я отметил последнюю привычку за сегодня")
    public void completedToday() throws Exception {
        completeForDate(ctx.getLastHabitId(), LocalDate.now().toString());
    }

    @Когда("я отмечаю последнюю привычку как выполненную за {string}")
    public void completeForDateStr(String date) throws Exception {
        completeForDate(ctx.getLastHabitId(), date);
    }

    @Допустим("я отметил последнюю привычку за {string}")
    public void completedForDate(String date) throws Exception {
        completeForDate(ctx.getLastHabitId(), date);
    }

    @Допустим("я отметил привычку {string} за {string}")
    public void completedNamedHabitForDate(String habitName, String date) throws Exception {
        Long habitId = ctx.getHabitNameToId().get(habitName);
        assertThat(habitId).as("Habit '%s' not found in context", habitName).isNotNull();
        completeForDate(habitId, date);
    }

    @Допустим("я отметил привычку {string} за сегодня")
    public void completedNamedHabitToday(String habitName) throws Exception {
        Long habitId = ctx.getHabitNameToId().get(habitName);
        assertThat(habitId).as("Habit '%s' not found in context", habitName).isNotNull();
        completeForDate(habitId, LocalDate.now().toString());
    }

    @Когда("я снимаю отметку за сегодня для последней привычки")
    public void uncompleteToday() throws Exception {
        String body = mapper.writeValueAsString(Map.of("date", LocalDate.now().toString()));

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/habits/" + ctx.getLastHabitId() + "/complete",
                HttpMethod.DELETE,
                new HttpEntity<>(body, authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());
    }

    @Когда("я запрашиваю отметки с {string} по {string}")
    public void getCompletions(String start, String end) {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/habits/" + ctx.getLastHabitId()
                        + "/completions?start=" + start + "&end=" + end,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());
    }

    // ===== Assertions =====

    @Тогда("текущий streak равен {int}")
    public void currentStreakEquals(int expected) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.get("currentStreak").asInt()).isEqualTo(expected);
    }

    @Тогда("ответ содержит perfectDay равный true")
    public void perfectDayIsTrue() throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.get("perfectDay").asBoolean()).isTrue();
    }

    @Тогда("в списке {int} даты")
    public void listHasDates(int count) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.size()).isEqualTo(count);
    }

    private void completeForDate(Long habitId, String date) throws Exception {
        String body = mapper.writeValueAsString(Map.of("date", date));

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/habits/" + habitId + "/complete",
                HttpMethod.POST,
                new HttpEntity<>(body, authHeaders()),
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
