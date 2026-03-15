package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.MascotMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MascotMessageRepository extends JpaRepository<MascotMessage, UUID> {

    Optional<MascotMessage> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
