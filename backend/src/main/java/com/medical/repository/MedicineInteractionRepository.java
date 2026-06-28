package com.medical.repository;

import com.medical.entity.MedicineInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicineInteractionRepository extends JpaRepository<MedicineInteraction, Long> {
    List<MedicineInteraction> findByMedicineIdAAndMedicineIdB(Long medicineIdA, Long medicineIdB);
}
