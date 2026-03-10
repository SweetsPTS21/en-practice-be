package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.UserPracticeRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPracticeRecommendationRepository extends JpaRepository<UserPracticeRecommendation, UUID> {

    Optional<UserPracticeRecommendation> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
