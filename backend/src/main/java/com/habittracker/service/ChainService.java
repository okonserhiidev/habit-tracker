package com.habittracker.service;

import com.habittracker.dto.request.ChainRequest;
import com.habittracker.model.Habit;
import com.habittracker.model.HabitChain;
import com.habittracker.model.HabitChainItem;
import com.habittracker.model.User;
import com.habittracker.repository.HabitChainRepository;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChainService {

    private final HabitChainRepository chainRepository;
    private final HabitRepository habitRepository;
    private final UserRepository userRepository;

    public List<HabitChain> getUserChains(Long userId) {
        return chainRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    @Transactional
    public HabitChain createChain(Long userId, ChainRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        HabitChain chain = HabitChain.builder()
                .user(user)
                .name(request.getName())
                .type(request.getType() != null ? request.getType() : "CUSTOM")
                .build();

        chain = chainRepository.save(chain);
        addItemsToChain(chain, request.getHabitIds(), userId);

        return chainRepository.save(chain);
    }

    @Transactional
    public HabitChain updateChain(Long chainId, Long userId, ChainRequest request) {
        HabitChain chain = chainRepository.findByIdAndUserId(chainId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Chain not found"));

        chain.setName(request.getName());
        if (request.getType() != null) {
            chain.setType(request.getType());
        }

        chain.getItems().clear();
        addItemsToChain(chain, request.getHabitIds(), userId);

        return chainRepository.save(chain);
    }

    @Transactional
    public void deleteChain(Long chainId, Long userId) {
        HabitChain chain = chainRepository.findByIdAndUserId(chainId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Chain not found"));
        chainRepository.delete(chain);
    }

    private void addItemsToChain(HabitChain chain, List<Long> habitIds, Long userId) {
        for (int i = 0; i < habitIds.size(); i++) {
            Habit habit = habitRepository.findByIdAndUserId(habitIds.get(i), userId)
                    .orElseThrow(() -> new IllegalArgumentException("Habit not found"));

            HabitChainItem item = HabitChainItem.builder()
                    .chain(chain)
                    .habit(habit)
                    .position(i)
                    .build();
            chain.getItems().add(item);
        }
    }
}
