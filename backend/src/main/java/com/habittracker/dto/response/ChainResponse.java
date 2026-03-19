package com.habittracker.dto.response;

import com.habittracker.model.HabitChain;
import com.habittracker.model.HabitChainItem;
import lombok.Builder;
import lombok.Data;

import java.util.Comparator;
import java.util.List;

@Data
@Builder
public class ChainResponse {
    private Long id;
    private String name;
    private String type;
    private List<ChainHabitItem> habits;

    @Data
    @Builder
    public static class ChainHabitItem {
        private Long habitId;
        private String habitName;
        private String habitColor;
        private int position;
    }

    public static ChainResponse from(HabitChain chain) {
        List<ChainHabitItem> items = chain.getItems().stream()
                .sorted(Comparator.comparingInt(HabitChainItem::getPosition))
                .map(item -> ChainHabitItem.builder()
                        .habitId(item.getHabit().getId())
                        .habitName(item.getHabit().getName())
                        .habitColor(item.getHabit().getColor())
                        .position(item.getPosition())
                        .build())
                .toList();

        return ChainResponse.builder()
                .id(chain.getId())
                .name(chain.getName())
                .type(chain.getType())
                .habits(items)
                .build();
    }
}
