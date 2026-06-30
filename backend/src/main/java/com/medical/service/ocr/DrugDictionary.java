package com.medical.service.ocr;

import com.medical.entity.Medicine;
import com.medical.repository.MedicineRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DrugDictionary {

    private final MedicineRepository medicineRepository;
    private List<String> drugNames = new ArrayList<>();
    private int maxDrugNameLength = 0;

    public DrugDictionary(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @PostConstruct
    public void init() {
        List<Medicine> allMedicines = medicineRepository.findAll();
        drugNames = allMedicines.stream()
                .map(Medicine::getMedicineName)
                .filter(name -> name != null && !name.isEmpty())
                .distinct()
                .sorted((a, b) -> b.length() - a.length())
                .collect(Collectors.toList());
        maxDrugNameLength = drugNames.stream().mapToInt(String::length).max().orElse(10);
        log.info("药品词典初始化完成，共 {} 种药品", drugNames.size());
    }

    /**
     * 从文本中匹配所有已知药品名（游标扫描，避免遗漏）
     */
    public List<String> matchAll(String text) {
        if (text == null || text.isEmpty()) return Collections.emptyList();
        Set<String> found = new LinkedHashSet<>();

        // 游标扫描 - 从每个位置开始匹配最长的药品名
        int i = 0;
        while (i < text.length()) {
            boolean matched = false;
            int maxLen = Math.min(maxDrugNameLength, text.length() - i);

            for (int len = maxLen; len >= 1; len--) {
                String sub = text.substring(i, i + len);
                if (drugNames.contains(sub)) {
                    found.add(sub);
                    i += len;
                    matched = true;
                    break;
                }
            }

            if (!matched) i++;
        }

        log.debug("药品匹配完成: {} -> {}", text, found);
        return new ArrayList<>(found);
    }

    /**
     * 从文本中匹配药品名及其上下文信息（用药详情）
     */
    public Map<String, String> matchWithDetails(String text) {
        if (text == null || text.isEmpty()) return Collections.emptyMap();
        Map<String, String> result = new LinkedHashMap<>();

        // 先找所有药品名
        List<String> matchedDrugs = matchAll(text);

        for (String drug : matchedDrugs) {
            int idx = text.indexOf(drug);
            if (idx >= 0) {
                // 提取药品后面的剂量信息（约20个字符）
                int end = Math.min(idx + drug.length() + 20, text.length());
                String context = text.substring(idx + drug.length(), end).trim();
                // 提取数字+单位模式作为剂量
                StringBuilder detail = new StringBuilder();
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+\\.?\\d*\\s*(mg|g|ml|片|粒|支|袋|瓶|次/日|次|天))").matcher(context);
                while (m.find()) {
                    if (detail.length() > 0) detail.append(" ");
                    detail.append(m.group(1));
                }
                result.put(drug, detail.toString());
            }
        }

        return result;
    }

    public List<String> getAllDrugNames() {
        return drugNames;
    }
}
