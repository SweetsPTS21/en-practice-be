package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.UserSmartReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSmartReminderRepository extends JpaRepository<UserSmartReminder, UUID> {

    Optional<UserSmartReminder> findByUserId(UUID userId);
}
