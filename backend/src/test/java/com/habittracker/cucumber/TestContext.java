package com.habittracker.cucumber;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared state between step definitions within a single scenario.
 */
@Component
public class TestContext {

    private String accessToken;
    private String refreshToken;
    private int lastStatusCode;
    private String lastResponseBody;
    private Long lastHabitId;
    private Long otherUserHabitId;
    private final Map<String, Long> habitNameToId = new HashMap<>();

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public int getLastStatusCode() { return lastStatusCode; }
    public void setLastStatusCode(int lastStatusCode) { this.lastStatusCode = lastStatusCode; }

    public String getLastResponseBody() { return lastResponseBody; }
    public void setLastResponseBody(String lastResponseBody) { this.lastResponseBody = lastResponseBody; }

    public Long getLastHabitId() { return lastHabitId; }
    public void setLastHabitId(Long lastHabitId) { this.lastHabitId = lastHabitId; }

    public Long getOtherUserHabitId() { return otherUserHabitId; }
    public void setOtherUserHabitId(Long otherUserHabitId) { this.otherUserHabitId = otherUserHabitId; }

    public Map<String, Long> getHabitNameToId() { return habitNameToId; }

    public void reset() {
        accessToken = null;
        refreshToken = null;
        lastStatusCode = 0;
        lastResponseBody = null;
        lastHabitId = null;
        otherUserHabitId = null;
        habitNameToId.clear();
    }
}
