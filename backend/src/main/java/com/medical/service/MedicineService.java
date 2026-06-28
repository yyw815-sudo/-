package com.medical.service;

import com.medical.entity.Medicine;
import com.medical.repository.MedicineRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;

    public MedicineService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    /** 获取所有药品 */
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    /** 根据ID获取药品 */
    public Medicine getMedicineById(Long id) {
        return medicineRepository.findById(id).orElse(null);
    }

    /** 搜索药品（模糊匹配名称） */
    public List<Medicine> searchMedicine(String keyword) {
        return medicineRepository.findByMedicineNameContaining(keyword);
    }

    /** 查找或创建药品（AI分析用） */
    public Medicine findOrCreateMedicine(String medicineName) {
        Medicine existing = medicineRepository.findByMedicineName(medicineName);
        if (existing != null) return existing;

        Medicine medicine = new Medicine();
        medicine.setMedicineName(medicineName);
        return medicineRepository.save(medicine);
    }
}
