package com.medical.repository;

import com.medical.entity.DrugInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DrugInteractionRepository extends JpaRepository<DrugInteraction, Long> {
    List<DrugInteraction> findByDrugNameAAndDrugNameB(String drugNameA, String drugNameB);
}
