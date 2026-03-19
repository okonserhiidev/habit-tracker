package com.habittracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ChainRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String type; // MORNING, EVENING, CUSTOM

    @NotEmpty(message = "At least one habit is required")
    private List<Long> habitIds; // ordered list of habit IDs
}
