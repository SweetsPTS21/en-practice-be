package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, UUID> {
}
