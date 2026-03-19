package com.habittracker.controller;

import com.habittracker.dto.request.ChainRequest;
import com.habittracker.dto.response.ChainResponse;
import com.habittracker.service.ChainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chains")
@RequiredArgsConstructor
public class ChainController {

    private final ChainService chainService;

    @GetMapping
    public List<ChainResponse> getChains(Authentication auth) {
        return chainService.getUserChains(getUserId(auth)).stream()
                .map(ChainResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChainResponse createChain(@Valid @RequestBody ChainRequest request, Authentication auth) {
        return ChainResponse.from(chainService.createChain(getUserId(auth), request));
    }

    @PutMapping("/{id}")
    public ChainResponse updateChain(
            @PathVariable Long id,
            @Valid @RequestBody ChainRequest request,
            Authentication auth) {
        return ChainResponse.from(chainService.updateChain(id, getUserId(auth), request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChain(@PathVariable Long id, Authentication auth) {
        chainService.deleteChain(id, getUserId(auth));
    }

    private Long getUserId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }
}
