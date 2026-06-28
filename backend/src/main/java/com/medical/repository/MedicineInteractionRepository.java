package com.medical.repository;

import com.medical.entity.MedicineInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicineInteractionRepository extends JpaRepository<MedicineInteraction, Long> {
    
    @Query("SELECT m FROM MedicineInteraction m WHERE (m.medicineA = :med1 AND m.medicineB = :med2) OR (m.medicineA = :med2 AND m.medicineB = :med1)")
    List<MedicineInteraction> findByMedicinePair(@Param("med1") Long med1, @Param("med2") Long med2);
    
    List<MedicineInteraction> findByMedicineA(Long medicineId);
    
    List<MedicineInteraction> findByMedicineB(Long medicineId);
}
