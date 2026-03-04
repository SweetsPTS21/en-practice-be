package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.IeltsSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IeltsSectionRepository extends JpaRepository<IeltsSection, UUID> {

    List<IeltsSection> findByTestIdOrderBySectionOrder(UUID testId);

    void deleteByTestId(UUID testId);
}
