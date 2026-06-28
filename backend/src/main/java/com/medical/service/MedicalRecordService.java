package com.medical.service;

import com.medical.entity.MedicalRecord;
import com.medical.repository.MedicalRecordRepository;
import com.medical.service.ocr.MedicalOcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class MedicalRecordService {

    private static final Logger log = LoggerFactory.getLogger(MedicalRecordService.class);
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalOcrService medicalOcrService;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                                 MedicalOcrService medicalOcrService) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.medicalOcrService = medicalOcrService;
    }

    /**
     * 分页查询病历
     */
    public Page<MedicalRecord> searchRecords(Long userId, String keyword,
                                              LocalDate startDate, LocalDate endDate,
                                              Pageable pageable) {
        if (keyword != null && !keyword.isBlank() && startDate != null && endDate != null) {
            return medicalRecordRepository.findByUserIdAndDiseaseNameContainingAndRecordDateBetween(
                    userId, keyword, startDate, endDate, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            return medicalRecordRepository.findByUserIdAndDiseaseNameContaining(userId, keyword, pageable);
        } else if (startDate != null && endDate != null) {
            return medicalRecordRepository.findByUserIdAndRecordDateBetween(userId, startDate, endDate, pageable);
        }
        return medicalRecordRepository.findByUserId(userId, pageable);
    }

    /**
     * 获取单个病历
     */
    public MedicalRecord getById(Long recordId) {
        return medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("病历不存在"));
    }

    /**
     * 创建病历
     */
    @Transactional
    public MedicalRecord createRecord(MedicalRecord record) {
        record.setRecordId(null);
        return medicalRecordRepository.save(record);
    }

    /**
     * 更新病历
     */
    @Transactional
    public MedicalRecord updateRecord(Long recordId, MedicalRecord updated) {
        MedicalRecord existing = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("病历不存在"));

        existing.setDiseaseName(updated.getDiseaseName());
        existing.setDiagnosis(updated.getDiagnosis());
        existing.setPrescription(updated.getPrescription());
        existing.setDoctor(updated.getDoctor());
        existing.setHospital(updated.getHospital());
        existing.setRecordDate(updated.getRecordDate());
        existing.setUpdateTime(LocalDateTime.now());

        return medicalRecordRepository.save(existing);
    }

    /**
     * 删除病历
     */
    @Transactional
    public void deleteRecord(Long recordId) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("病历不存在"));
        medicalRecordRepository.delete(record);
    }

    /**
     * 核心算法：通用文字识别 + FSME空间匹配 + Regex规则 + LLM纠错 → 返回结构化病历信息和原始结果
     */
    public MedicalRecord recognizeTextAndParse(byte[] imageBytes) {
        log.info("开始核心算法识别流程...");
        return buildRecordFromRecognition(medicalOcrService.recognize(imageBytes));
    }

    /**
     * 核心算法（返回完整识别结果）
     */
    public Map<String, Object> recognizeTextAndParseFull(byte[] imageBytes) {
        log.info("开始核心算法识别流程（完整结果）...");
        MedicalOcrService.RecognitionResult result = medicalOcrService.recognize(imageBytes);
        MedicalRecord record = buildRecordFromRecognition(result);

        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("record", record);
        response.put("rawText", result.getRawText());
        response.put("fields", result.getFields());
        response.put("scores", result.getScores());
        response.put("sources", result.getSources());
        response.put("needCorrection", result.isNeedCorrection());
        return response;
    }

    private MedicalRecord buildRecordFromRecognition(MedicalOcrService.RecognitionResult result) {
        MedicalRecord record = new MedicalRecord();

        Map<String, String> fields = result.getFields();
        
        // 字段映射
        if (fields.containsKey("诊断")) {
            record.setDiseaseName(fields.get("诊断"));
            record.setDiagnosis(fields.get("诊断"));
        }
        if (fields.containsKey("diagnosis")) {
            record.setDiagnosis(fields.get("diagnosis"));
            if (record.getDiseaseName() == null) {
                record.setDiseaseName(fields.get("diagnosis"));
            }
        }
        // 2026-06-28: 新增字段映射
        if (fields.containsKey("性别") || fields.containsKey("gender")) {
            record.setGender(fields.getOrDefault("性别", fields.get("gender")));
        }
        if (fields.containsKey("年龄") || fields.containsKey("age")) {
            String ageStr = fields.getOrDefault("年龄", fields.get("age"));
            try {
                record.setAge(Integer.parseInt(ageStr.replaceAll("[^0-9]", "")));
            } catch (Exception ignored) {}
        }
        if (fields.containsKey("既往史") || fields.containsKey("pastHistory")) {
            record.setPastHistory(fields.getOrDefault("既往史", fields.get("pastHistory")));
        }
        if (fields.containsKey("现病史") || fields.containsKey("presentHistory")) {
            record.setPresentHistory(fields.getOrDefault("现病史", fields.get("presentHistory")));
        }
        if (fields.containsKey("主诉") || fields.containsKey("chiefComplaint")) {
            record.setChiefComplaint(fields.getOrDefault("主诉", fields.get("chiefComplaint")));
        }
        if (fields.containsKey("处理意见") || fields.containsKey("treatment")) {
            record.setTreatment(fields.getOrDefault("处理意见", fields.get("treatment")));
        }

        if (fields.containsKey("姓名") || fields.containsKey("name")) {
            String name = fields.getOrDefault("姓名", fields.get("name"));
            if (record.getDiseaseName() == null) {
                record.setDiseaseName(name);
            }
        }
        if (fields.containsKey("医生") || fields.containsKey("doctor")) {
            record.setDoctor(fields.getOrDefault("医生", fields.get("doctor")));
        }
        if (fields.containsKey("医院") || fields.containsKey("hospital")) {
            record.setHospital(fields.getOrDefault("医院", fields.get("hospital")));
        }
        // 日期处理：从所有字段中找出最可能的就诊日期
        // 规则：遍历所有日期值，取最接近今天（且不晚于今天）的日期
        Integer patientAge = record.getAge(); // 已从fields中提取的年龄
        LocalDate bestDate = null;
        int bestScore = -1; // 日期优先级：visitDate > 日期 > birthday > 其他
        for (Map.Entry<String, String> f : fields.entrySet()) {
            String key = f.getKey();
            String val = f.getValue();
            LocalDate parsed = parseDate(val);
            if (parsed == null) continue;

            // 出生日期检测：如果年份 ≈ 当前年份-年龄，说明是出生日期，降优先级
            boolean looksLikeBirthday = false;
            if (patientAge != null && patientAge > 0) {
                int birthYearGuess = LocalDate.now().getYear() - patientAge;
                if (Math.abs(parsed.getYear() - birthYearGuess) <= 2) {
                    looksLikeBirthday = true;
                }
            }

            int score;
            if (key.equals("visitDate"))        score = 100; // LLM识别的就诊日期，最优先
            else if (key.equals("日期"))         score = 80;  // FSM匹配的日期
            else if (key.equals("birthday"))     score = 50;  // LLM识别的"生日"
            else if (key.contains("出生") || key.contains("生日")) score = 10; // 真正的出生日期
            else                                 score = 40;  // 其他未知日期字段

            // 如果看起来像出生日期，大幅降权
            if (looksLikeBirthday) score = Math.min(score, 5);

            if (score > bestScore && !parsed.isAfter(LocalDate.now())) {
                bestScore = score;
                bestDate = parsed;
            }
        }
        if (bestDate != null) {
            record.setRecordDate(bestDate);
        } else {
            record.setRecordDate(LocalDate.now());
        }
        if (fields.containsKey("处方") || fields.containsKey("medicines") || fields.containsKey("药品")) {
            String drugInfo = fields.getOrDefault("处方", fields.getOrDefault("medicines", fields.getOrDefault("药品", "")));
            // 将药品信息追加到处理意见中
            String currentTreatment = record.getTreatment();
            if (currentTreatment != null && !currentTreatment.isEmpty()) {
                record.setTreatment(currentTreatment + "\n【药品】" + drugInfo);
            } else {
                record.setTreatment("【药品】" + drugInfo);
            }
        }

        if (record.getDiseaseName() == null) {
            record.setDiseaseName("未知疾病");
        }
        
        log.info("核心算法识别完成，字段: {}", fields);
        return record;
    }

    /**
     * 解析日期字符串
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        String[] patterns = {"yyyy-MM-dd", "yyyy/MM/dd", "yyyy年MM月dd日", "yyyy.MM.dd", "yyyyMMdd"};
        for (String p : patterns) {
            try {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(p);
                return LocalDate.parse(dateStr.trim(), formatter);
            } catch (Exception ignored) {}
        }
        try {
            if (dateStr.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
                return LocalDate.parse(dateStr.trim());
            }
        } catch (Exception ignored) {}
        return null;
    }
}
