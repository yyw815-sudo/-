package com.medical.service.ai;

import com.medical.service.ai.context.PatientContext;
import com.medical.service.ocr.DrugDictionary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class MedicalParser {

    private final DrugDictionary drugDictionary;

    public MedicalParser(DrugDictionary drugDictionary) {
        this.drugDictionary = drugDictionary;
    }

    public TreatmentContext parse(PatientContext context) {
        TreatmentContext treatment = new TreatmentContext();
        String rawText = context.getDiagnoses() != null ? String.join(" ", context.getDiagnoses()) : "";

        List<String> matchedDrugs = drugDictionary.matchAll(rawText);
        for (String drugName : matchedDrugs) {
            TreatmentContext.MedicineItem item = new TreatmentContext.MedicineItem();
            item.setName(drugName);
            treatment.getMedicines().add(item);
        }

        // 从药品字段解析（药品信息已与处理意见分离）
        String medicines = context.getMedicines();
        if (medicines != null && !medicines.isEmpty()) {
            List<String> drugFromMedicines = drugDictionary.matchAll(medicines);
            for (String drugName : drugFromMedicines) {
                boolean exists = treatment.getMedicines().stream()
                        .anyMatch(m -> m.getName().equals(drugName));
                if (!exists) {
                    TreatmentContext.MedicineItem item = new TreatmentContext.MedicineItem();
                    item.setName(drugName);
                    treatment.getMedicines().add(item);
                }
            }
        }

        log.info("病历解析完成，药品数={}（来自诊断={}，来自药品字段={}）",
                treatment.getMedicines().size(), matchedDrugs.size(),
                medicines != null && !medicines.isEmpty()
                        ? drugDictionary.matchAll(medicines).size() : 0);
        return treatment;
    }
}
