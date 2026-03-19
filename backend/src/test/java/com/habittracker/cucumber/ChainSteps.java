package com.habittracker.cucumber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.ru.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ChainSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext ctx;

    private final ObjectMapper mapper = new ObjectMapper();
    private Long lastChainId;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Когда("я создаю цепочку {string} типа {string} с привычками {string}")
    @Допустим("я создал цепочку {string} типа {string} с привычками {string}")
    public void createChain(String name, String type, String habitNames) throws Exception {
        List<Long> habitIds = Arrays.stream(habitNames.split(","))
                .map(String::trim)
                .map(n -> ctx.getHabitNameToId().get(n))
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("type", type);
        body.put("habitIds", habitIds);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/chains",
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(body), authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode json = mapper.readTree(response.getBody());
            lastChainId = json.get("id").asLong();
        }
    }

    @Когда("я запрашиваю список цепочек")
    public void getChains() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/chains",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());
    }

    @Когда("я обновляю последнюю цепочку на {string} типа {string} с привычками {string}")
    public void updateChain(String name, String type, String habitNames) throws Exception {
        List<Long> habitIds = Arrays.stream(habitNames.split(","))
                .map(String::trim)
                .map(n -> ctx.getHabitNameToId().get(n))
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("type", type);
        body.put("habitIds", habitIds);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/chains/" + lastChainId,
                HttpMethod.PUT,
                new HttpEntity<>(mapper.writeValueAsString(body), authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());
    }

    @Когда("я удаляю последнюю цепочку")
    public void deleteChain() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/chains/" + lastChainId,
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders()),
                String.class);

        ctx.setLastStatusCode(response.getStatusCode().value());
        ctx.setLastResponseBody(response.getBody());
    }

    @Тогда("цепочка содержит {int} привычки")
    public void chainHasHabits(int count) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.get("habits").size()).isEqualTo(count);
    }

    @Тогда("первая привычка в цепочке это {string}")
    public void firstHabitInChain(String name) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.get("habits").get(0).get("habitName").asText()).isEqualTo(name);
    }

    @Тогда("в списке цепочек {int} элемент")
    public void chainsListSize(int count) throws Exception {
        JsonNode json = mapper.readTree(ctx.getLastResponseBody());
        assertThat(json.size()).isEqualTo(count);
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + ctx.getAccessToken());
        return headers;
    }
}
