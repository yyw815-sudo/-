package com.medical.repository;

import com.medical.entity.AiMedicationPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiMedicationPlanRepository extends JpaRepository<AiMedicationPlan, Long> {

    @Query(value = "SELECT * FROM ai_medication_plan WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "plan_name LIKE CONCAT('%', :keyword, '%') OR disease_type LIKE CONCAT('%', :keyword, '%')) AND " +
            "(:status IS NULL OR status = :status)",
            countQuery = "SELECT COUNT(*) FROM ai_medication_plan WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR " +
                    "plan_name LIKE CONCAT('%', :keyword, '%') OR disease_type LIKE CONCAT('%', :keyword, '%')) AND " +
                    "(:status IS NULL OR status = :status)",
            nativeQuery = true)
    Page<AiMedicationPlan> findByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);

    @Query(value = "SELECT * FROM ai_medication_plan WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "plan_name LIKE CONCAT('%', :keyword, '%') OR disease_type LIKE CONCAT('%', :keyword, '%'))",
            countQuery = "SELECT COUNT(*) FROM ai_medication_plan WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR " +
                    "plan_name LIKE CONCAT('%', :keyword, '%') OR disease_type LIKE CONCAT('%', :keyword, '%'))",
            nativeQuery = true)
    Page<AiMedicationPlan> findByKeywordOnly(@Param("keyword") String keyword, Pageable pageable);

    List<AiMedicationPlan> findByUserId(Long userId);

    List<AiMedicationPlan> findByStatusOrderByCreateTimeDesc(Integer status);
}