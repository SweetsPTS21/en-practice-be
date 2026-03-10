package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.UserDailyXpCap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDailyXpCapRepository extends JpaRepository<UserDailyXpCap, UUID> {
    
    Optional<UserDailyXpCap> findByUserIdAndDate(UUID userId, LocalDate date);
}
