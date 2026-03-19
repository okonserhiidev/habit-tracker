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

public class FreezeDaySteps {

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

    @Когда("я замораживаю вчерашний день для последней привычки")
    public void freezeYesterday() throws Exception {
        freezeDate(LocalDate.now().minusDays(1).toString());
    }

    @Когда("я замораживаю завтрашний день для последней привычки")
    public void freezeTomorrow() throws Exception {
        freezeDate(LocalDate.now().plusDays(1).toString());
    }

    @Когда("я замораживаю день {string} для последней привычки")
    public void freezeRelativeDay(String offset) throws Exception {
        LocalDate date = LocalDate.now().plusDays(Integer.parseInt(offset));
        freezeDate(date.toString());
    }

    @Когда("я запрашиваю список freeze days")
    public void getFreezeDays() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/freeze-days",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());
    }

    @Тогда("ответ содержит freezeDate")
    public void responseContainsFreezeDate() throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.has("freezeDate")).isTrue();
        assertThat(json.get("freezeDate").asText()).isNotEmpty();
    }

    @Тогда("ответ содержит remainingThisWeek")
    public void responseContainsRemaining() throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.has("remainingThisWeek")).isTrue();
    }

    private void freezeDate(String date) throws Exception {
        String body = mapper.writeValueAsString(Map.of("date", date));

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/habits/" + ctx.getLastHabitId() + "/freeze",
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
