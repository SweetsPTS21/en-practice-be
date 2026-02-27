package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByUserId(UUID userId);

    List<FcmToken> findByUserIdIn(List<UUID> userIds);

    Optional<FcmToken> findByToken(String token);

    boolean existsByToken(String token);

    @Modifying
    @Query("DELETE FROM FcmToken f WHERE f.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM FcmToken f WHERE f.token = :token")
    void deleteByToken(@Param("token") String token);
}
