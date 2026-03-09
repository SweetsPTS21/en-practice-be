package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByCreatedAtAfter(Instant after);

    List<User> findByRole(com.swpts.enpracticebe.constant.Role role);
}
