package com.habittracker.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CompletionRequest {
    @NotNull
    private LocalDate date;
}
