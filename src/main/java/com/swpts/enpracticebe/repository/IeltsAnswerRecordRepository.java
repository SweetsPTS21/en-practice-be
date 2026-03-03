package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.IeltsAnswerRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IeltsAnswerRecordRepository extends JpaRepository<IeltsAnswerRecord, UUID> {

    List<IeltsAnswerRecord> findByAttemptId(UUID attemptId);
}
