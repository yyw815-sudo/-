package com.medical.repository;

import com.medical.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByMedicineName(String medicineName);
    List<Medicine> findByMedicineNameContaining(String keyword);
    List<Medicine> findByIndicationContaining(String keyword);
}
