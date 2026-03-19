package com.habittracker.service;

import com.habittracker.dto.request.ChainRequest;
import com.habittracker.model.Habit;
import com.habittracker.model.HabitChain;
import com.habittracker.model.User;
import com.habittracker.repository.HabitChainRepository;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChainServiceTest {

    @Mock private HabitChainRepository chainRepository;
    @Mock private HabitRepository habitRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private ChainService chainService;

    private User testUser;
    private Habit habit1;
    private Habit habit2;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("test@test.com").authProvider(User.AuthProvider.LOCAL).build();
        habit1 = Habit.builder().id(1L).user(testUser).name("Running").color("#4CAF50").build();
        habit2 = Habit.builder().id(2L).user(testUser).name("Reading").color("#2196F3").build();
    }

    // === createChain ===

    @Test
    void createChain_success_returnsChainWithItems() {
        ChainRequest request = new ChainRequest();
        request.setName("Morning Ritual");
        request.setType("MORNING");
        request.setHabitIds(List.of(1L, 2L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(habit1));
        when(habitRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(habit2));
        when(chainRepository.save(any(HabitChain.class))).thenAnswer(inv -> {
            HabitChain chain = inv.getArgument(0);
            chain.setId(1L);
            return chain;
        });

        HabitChain result = chainService.createChain(1L, request);

        assertThat(result.getName()).isEqualTo("Morning Ritual");
        assertThat(result.getType()).isEqualTo("MORNING");
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getHabit().getName()).isEqualTo("Running");
        assertThat(result.getItems().get(0).getPosition()).isEqualTo(0);
        assertThat(result.getItems().get(1).getHabit().getName()).isEqualTo("Reading");
        assertThat(result.getItems().get(1).getPosition()).isEqualTo(1);
    }

    @Test
    void createChain_defaultType_setCustom() {
        ChainRequest request = new ChainRequest();
        request.setName("My Chain");
        request.setHabitIds(List.of(1L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(habitRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(habit1));
        when(chainRepository.save(any(HabitChain.class))).thenAnswer(inv -> {
            HabitChain chain = inv.getArgument(0);
            chain.setId(1L);
            return chain;
        });

        HabitChain result = chainService.createChain(1L, request);

        assertThat(result.getType()).isEqualTo("CUSTOM");
    }

    @Test
    void createChain_habitNotFound_throwsException() {
        ChainRequest request = new ChainRequest();
        request.setName("Chain");
        request.setHabitIds(List.of(99L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(chainRepository.save(any(HabitChain.class))).thenAnswer(inv -> {
            HabitChain chain = inv.getArgument(0);
            chain.setId(1L);
            return chain;
        });
        when(habitRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chainService.createChain(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Habit not found");
    }

    // === updateChain ===

    @Test
    void updateChain_success_updatesNameAndItems() {
        HabitChain existing = HabitChain.builder()
                .id(1L).user(testUser).name("Old").type("MORNING")
                .items(new ArrayList<>()).build();

        ChainRequest request = new ChainRequest();
        request.setName("Updated");
        request.setType("EVENING");
        request.setHabitIds(List.of(2L));

        when(chainRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existing));
        when(habitRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(habit2));
        when(chainRepository.save(any(HabitChain.class))).thenAnswer(inv -> inv.getArgument(0));

        HabitChain result = chainService.updateChain(1L, 1L, request);

        assertThat(result.getName()).isEqualTo("Updated");
        assertThat(result.getType()).isEqualTo("EVENING");
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    void updateChain_notFound_throwsException() {
        ChainRequest request = new ChainRequest();
        request.setName("X");
        request.setHabitIds(List.of(1L));

        when(chainRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chainService.updateChain(99L, 1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Chain not found");
    }

    // === deleteChain ===

    @Test
    void deleteChain_success_deletesChain() {
        HabitChain chain = HabitChain.builder().id(1L).user(testUser).build();
        when(chainRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(chain));

        chainService.deleteChain(1L, 1L);

        verify(chainRepository).delete(chain);
    }

    @Test
    void deleteChain_notFound_throwsException() {
        when(chainRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chainService.deleteChain(99L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Chain not found");
    }

    // === getUserChains ===

    @Test
    void getUserChains_returnsList() {
        HabitChain chain = HabitChain.builder().id(1L).name("Morning").build();
        when(chainRepository.findByUserIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(chain));

        List<HabitChain> result = chainService.getUserChains(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Morning");
    }
}
