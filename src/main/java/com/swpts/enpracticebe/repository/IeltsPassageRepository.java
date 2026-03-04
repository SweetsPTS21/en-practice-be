package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.IeltsPassage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IeltsPassageRepository extends JpaRepository<IeltsPassage, UUID> {

    List<IeltsPassage> findBySectionIdOrderByPassageOrder(UUID sectionId);

    List<IeltsPassage> findBySectionIdIn(List<UUID> sectionIds);

    void deleteBySectionIdIn(List<UUID> sectionIds);
}
