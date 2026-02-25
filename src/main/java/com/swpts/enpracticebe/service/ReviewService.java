package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.ReviewSessionRequest;
import com.swpts.enpracticebe.entity.ReviewSession;
import com.swpts.enpracticebe.repository.ReviewSessionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ReviewService {
    private final ReviewSessionRepository reviewSessionRepository;

    public ReviewSession createSession(UUID userId, ReviewSessionRequest request) {
        ReviewSession session = ReviewSession.builder()
                .userId(userId)
                .filter(request.getFilter())
                .total(request.getTotal())
                .correct(request.getCorrect())
                .incorrect(request.getIncorrect())
                .accuracy(request.getAccuracy())
                .words(request.getWords())
                .build();
        return reviewSessionRepository.save(session);
    }

    public Optional<ReviewSession> getLastSession(UUID userId) {
        return reviewSessionRepository.findSecondLatest(userId);
    }
}
