package com.medical.repository;

import com.medical.entity.AiAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AiAnalysisResultRepository extends JpaRepository<AiAnalysisResult, Long> {
    Optional<AiAnalysisResult> findFirstByRecordIdOrderByVersionDesc(Long recordId);
    List<AiAnalysisResult> findByUserIdOrderByAnalysisTimeDesc(Long userId);
    List<AiAnalysisResult> findByRecordId(Long recordId);
    void deleteByRecordId(Long recordId);
}
