package com.habittracker.cucumber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HabitSteps {

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

    @Когда("я создаю привычку:")
    public void createHabitFromTable(DataTable table) throws Exception {
        Map<String, String> data = table.asMap(String.class, String.class);
        Map<String, String> body = new HashMap<>(data);
        if (!body.containsKey("color")) body.put("color", "#000000");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/habits",
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(body), authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode json = mapper.readTree(response.getBody());
            ctx.setLastHabitId(json.get("id").asLong());
            if (json.has("name")) {
                ctx.getHabitNameToId().put(json.get("name").asText(), json.get("id").asLong());
            }
        }
    }

    @Допустим("я создал привычку {string} с цветом {string}")
    public void createSimpleHabit(String name, String color) throws Exception {
        Map<String, String> body = Map.of("name", name, "color", color);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/habits",
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(body), authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode json = mapper.readTree(response.getBody());
            ctx.setLastHabitId(json.get("id").asLong());
            ctx.getHabitNameToId().put(name, json.get("id").asLong());
        }
    }

    @Когда("я запрашиваю список привычек")
    public void getHabits() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/habits",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());
    }

    @Когда("я обновляю последнюю привычку:")
    public void updateHabit(DataTable table) throws Exception {
        Map<String, String> data = table.asMap(String.class, String.class);
        Map<String, String> body = new HashMap<>(data);
        if (!body.containsKey("color")) body.put("color", "#000000");
        if (!body.containsKey("name")) body.put("name", "Default");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/habits/" + ctx.getLastHabitId(),
                HttpMethod.PUT,
                new HttpEntity<>(mapper.writeValueAsString(body), authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());
    }

    @Когда("я удаляю последнюю привычку")
    public void deleteHabit() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/habits/" + ctx.getLastHabitId(),
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());
    }

    @Когда("я архивирую последнюю привычку")
    public void archiveHabit() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/habits/" + ctx.getLastHabitId() + "/archive",
                HttpMethod.PATCH,
                new HttpEntity<>(authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());
    }

    @Когда("я меняю порядок привычек на обратный")
    public void reorderHabits() throws Exception {
        // Get current habits
        ResponseEntity<String> listResponse = restTemplate.exchange(
                baseUrl() + "/api/habits",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                String.class);

        JsonNode habits = mapper.readTree(listResponse.getBody());
        java.util.List<Long> ids = new java.util.ArrayList<>();
        for (JsonNode h : habits) ids.add(h.get("id").asLong());
        java.util.Collections.reverse(ids);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/habits/reorder",
                HttpMethod.PUT,
                new HttpEntity<>(mapper.writeValueAsString(ids), authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());
    }

    @Допустим("существует другой пользователь {string} с привычкой {string}")
    public void otherUserWithHabit(String email, String habitName) throws Exception {
        // Register other user
        String regBody = mapper.writeValueAsString(
                Map.of("email", email, "password", "password123", "name", "Other"));
        ResponseEntity<String> regResponse = restTemplate.exchange(
                baseUrl() + "/api/auth/register",
                HttpMethod.POST,
                new HttpEntity<>(regBody, jsonHeaders()),
                String.class);

        String otherToken = mapper.readTree(regResponse.getBody()).get("accessToken").asText();

        // Create habit for other user
        HttpHeaders otherHeaders = new HttpHeaders();
        otherHeaders.setContentType(MediaType.APPLICATION_JSON);
        otherHeaders.set("Authorization", "Bearer " + otherToken);

        String habitBody = mapper.writeValueAsString(Map.of("name", habitName, "color", "#000000"));
        ResponseEntity<String> habitResponse = restTemplate.exchange(
                baseUrl() + "/api/habits",
                HttpMethod.POST,
                new HttpEntity<>(habitBody, otherHeaders),
                String.class);

        ctx.setOtherUserHabitId(mapper.readTree(habitResponse.getBody()).get("id").asLong());
    }

    @Когда("я пытаюсь получить чужую привычку")
    public void getOtherUsersHabit() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/habits/" + ctx.getOtherUserHabitId(),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());
    }

    // ===== Assertions =====

    @Тогда("привычка имеет название {string}")
    public void habitHasName(String name) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.get("name").asText()).isEqualTo(name);
    }

    @Тогда("привычка имеет цвет {string}")
    public void habitHasColor(String color) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.get("color").asText()).isEqualTo(color);
    }

    @Тогда("привычка имеет идентичность {string}")
    public void habitHasIdentity(String identity) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.get("identityText").asText()).isEqualTo(identity);
    }

    @Тогда("привычка имеет мини-версию {string}")
    public void habitHasMiniVersion(String miniVersion) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.get("miniVersion").asText()).isEqualTo(miniVersion);
    }

    @Тогда("привычка имеет частоту {string}")
    public void habitHasFrequency(String frequency) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.get("frequency").asText()).isEqualTo(frequency);
    }

    @Тогда("привычка архивирована")
    public void habitIsArchived() throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.get("isArchived").asBoolean()).isTrue();
    }

    @Тогда("в списке минимум {int} привычки")
    public void listHasMinHabits(int count) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.size()).isGreaterThanOrEqualTo(count);
    }

    @Тогда("в списке {int} привычек")
    public void listHasExactHabits(int count) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.size()).isEqualTo(count);
    }

    @Тогда("каждая привычка содержит поле completedToday")
    public void eachHabitHasCompletedToday() throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        for (JsonNode habit : json) {
            assertThat(habit.has("completedToday")).isTrue();
        }
    }

    @Тогда("каждая привычка содержит поле currentStreak")
    public void eachHabitHasCurrentStreak() throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        for (JsonNode habit : json) {
            assertThat(habit.has("currentStreak")).isTrue();
        }
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
