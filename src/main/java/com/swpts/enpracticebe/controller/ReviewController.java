package com.swpts.enpracticebe.controller;

import com.swpts.enpracticebe.dto.request.ReviewSessionRequest;
import com.swpts.enpracticebe.entity.ReviewSession;
import com.swpts.enpracticebe.service.ReviewService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewSession> createSession(Authentication auth,
                                                       @Valid @RequestBody ReviewSessionRequest request) {
        UUID userId = (UUID) auth.getPrincipal();
        ReviewSession session = reviewService.createSession(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @GetMapping("/last")
    public ResponseEntity<ReviewSession> getLastSession(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return reviewService.getLastSession(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(null));
    }
}
