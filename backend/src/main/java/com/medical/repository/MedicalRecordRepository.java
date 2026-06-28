package com.medical.repository;

import com.medical.entity.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByUserIdOrderByRecordDateDesc(Long userId);

    Page<MedicalRecord> findByUserId(Long userId, Pageable pageable);

    Page<MedicalRecord> findByUserIdAndDiseaseNameContaining(Long userId, String keyword, Pageable pageable);

    Page<MedicalRecord> findByUserIdAndRecordDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<MedicalRecord> findByUserIdAndDiseaseNameContainingAndRecordDateBetween(
            Long userId, String keyword, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
