package com.medical.controller;

import com.medical.dto.Result;
import com.medical.entity.Medicine;
import com.medical.service.MedicineService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medicine")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping("/list")
    public Result<List<Medicine>> getAllMedicines() {
        return Result.success(medicineService.getAllMedicines());
    }

    @GetMapping("/{id}")
    public Result<Medicine> getMedicineById(@PathVariable Long id) {
        Medicine medicine = medicineService.getMedicineById(id);
        return medicine != null ? Result.success(medicine) : Result.error(404, "药品不存在");
    }

    @GetMapping("/search")
    public Result<List<Medicine>> searchMedicine(@RequestParam String keyword) {
        return Result.success(medicineService.searchMedicine(keyword));
    }
}
