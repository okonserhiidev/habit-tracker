package com.habittracker.dto.response;

import com.habittracker.model.FreezeDay;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class FreezeDayResponse {
    private Long id;
    private Long habitId;
    private String habitName;
    private LocalDate freezeDate;

    public static FreezeDayResponse from(FreezeDay fd) {
        return FreezeDayResponse.builder()
                .id(fd.getId())
                .habitId(fd.getHabit().getId())
                .habitName(fd.getHabit().getName())
                .freezeDate(fd.getFreezeDate())
                .build();
    }
}
