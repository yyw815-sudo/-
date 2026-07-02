package com.medical.data;

import com.medical.entity.AiMedicationPlan;
import com.medical.entity.OcrReview;
import com.medical.entity.PillRecognitionReview;
import com.medical.entity.User;
import com.medical.repository.AiMedicationPlanRepository;
import com.medical.repository.OcrReviewRepository;
import com.medical.repository.PillRecognitionReviewRepository;
import com.medical.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
public class TestDataGenerator {

    @Bean
    public CommandLineRunner generateAiCenterTestData(
            AiMedicationPlanRepository medicationPlanRepository,
            OcrReviewRepository ocrReviewRepository,
            PillRecognitionReviewRepository pillReviewRepository,
            UserRepository userRepository) {
        return args -> {
            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
                System.out.println("未找到用户数据，先创建测试用户...");
                User testUser = new User();
                testUser.setUserName("testuser");
                testUser.setPassword("123456");
                testUser.setRealname("测试用户");
                testUser.setPhone("13800138000");
                users = List.of(userRepository.save(testUser));
            }
            Long userId = users.get(0).getUserId();
            
            generateMedicationPlanData(medicationPlanRepository, userId);
            generateOcrReviewData(ocrReviewRepository, userId);
            generatePillReviewData(pillReviewRepository, userId);
            
            System.out.println("AI中心测试数据生成完成！");
        };
    }

    private void generateMedicationPlanData(AiMedicationPlanRepository repository, Long userId) {
        if (repository.count() > 0) {
            System.out.println("AI用药计划表已有数据，跳过生成");
            return;
        }
        
        String[] diseaseTypes = {"高血压", "糖尿病", "冠心病", "高血脂", "哮喘"};
        String[] planNames = {"高血压日常用药方案", "2型糖尿病治疗计划", "冠心病二级预防方案", "高血脂降脂计划", "哮喘控制计划"};
        String[] medicationLists = {
            "[{\"name\":\"硝苯地平控释片\",\"dose\":\"每日1次，每次1片\",\"time\":\"早晨\"},{\"name\":\"缬沙坦胶囊\",\"dose\":\"每日1次，每次1粒\",\"time\":\"晚上\"}]",
            "[{\"name\":\"二甲双胍缓释片\",\"dose\":\"每日2次，每次1片\",\"time\":\"早晚\"},{\"name\":\"格列齐特缓释片\",\"dose\":\"每日1次，每次1片\",\"time\":\"早餐前\"}]",
            "[{\"name\":\"阿司匹林肠溶片\",\"dose\":\"每日1次，每次1片\",\"time\":\"早晨\"},{\"name\":\"阿托伐他汀钙片\",\"dose\":\"每日1次，每次1片\",\"time\":\"晚上\"}]",
            "[{\"name\":\"瑞舒伐他汀钙片\",\"dose\":\"每日1次，每次1片\",\"time\":\"晚上\"},{\"name\":\"依折麦布片\",\"dose\":\"每日1次，每次1片\",\"time\":\"早晨\"}]",
            "[{\"name\":\"沙美特罗替卡松粉吸入剂\",\"dose\":\"每日2次，每次1吸\",\"time\":\"早晚\"},{\"name\":\"孟鲁司特钠片\",\"dose\":\"每日1次，每次1片\",\"time\":\"晚上\"}]"
        };
        String[] dosageInstructions = {
            "请严格按照医嘱服药，定期监测血压，避免情绪激动和过度劳累。",
            "注意饮食控制，定期监测血糖，避免高糖高脂食物。",
            "坚持规律运动，控制体重，定期复查心电图和血脂。",
            "低脂饮食，适当运动，定期复查血脂四项。",
            "避免接触过敏原，随身携带急救药物，定期肺功能检查。"
        };
        String[] aiAnalyses = {
            "AI分析：该方案采用钙通道阻滞剂联合ARB类药物，适合中度高血压患者，建议定期监测肾功能。",
            "AI分析：该方案采用双胍类联合磺脲类药物，适合2型糖尿病患者，注意低血糖风险。",
            "AI分析：该方案包含抗血小板和降脂药物，符合冠心病二级预防指南，建议长期坚持。",
            "AI分析：该方案采用他汀类联合胆固醇吸收抑制剂，降脂效果显著，注意肝功能监测。",
            "AI分析：该方案采用吸入糖皮质激素联合长效β2受体激动剂，适合中度持续性哮喘患者。"
        };
        
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 0; i < 5; i++) {
            AiMedicationPlan plan = new AiMedicationPlan();
            plan.setUserId(userId);
            plan.setPlanName(planNames[i]);
            plan.setDiseaseType(diseaseTypes[i]);
            plan.setMedicationList(medicationLists[i]);
            plan.setDosageInstructions(dosageInstructions[i]);
            plan.setStartDate(today.minusDays(30));
            plan.setEndDate(today.plusDays(90));
            plan.setStatus(i % 3);
            plan.setAiAnalysis(aiAnalyses[i]);
            plan.setCreateTime(LocalDateTime.parse(today.minusDays(i).atTime(10, 0, 0).format(formatter), formatter));
            plan.setUpdateTime(plan.getCreateTime());
            
            repository.save(plan);
        }
        System.out.println("生成了5条AI用药计划测试数据");
    }

    private void generateOcrReviewData(OcrReviewRepository repository, Long userId) {
        if (repository.count() > 0) {
            System.out.println("OCR审核表已有数据，跳过生成");
            return;
        }
        
        String[] medicationNames = {"硝苯地平控释片", "二甲双胍缓释片", "阿司匹林肠溶片", "阿托伐他汀钙片", "氯吡格雷片", "美托洛尔缓释片", "缬沙坦胶囊", "格列齐特缓释片"};
        String[] dosages = {"每日1次，每次1片", "每日2次，每次1片", "每日1次，每次1片", "每日1次，每次1片", "每日1次，每次1片", "每日1次，每次半片", "每日1次，每次1粒", "每日1次，每次1片"};
        String[] ocrResults = {
            "药品名称：硝苯地平控释片\n规格：30mg\n用法：每日1次，每次1片\n有效期：2025-12-31",
            "药品名称：二甲双胍缓释片\n规格：500mg\n用法：每日2次，每次1片\n有效期：2026-06-30",
            "药品名称：阿司匹林肠溶片\n规格：100mg\n用法：每日1次，每次1片\n有效期：2025-09-30",
            "药品名称：阿托伐他汀钙片\n规格：20mg\n用法：每日1次，每次1片\n有效期：2026-03-31",
            "药品名称：氯吡格雷片\n规格：75mg\n用法：每日1次，每次1片\n有效期：2025-11-30",
            "药品名称：美托洛尔缓释片\n规格：47.5mg\n用法：每日1次，每次半片\n有效期：2026-01-31",
            "药品名称：缬沙坦胶囊\n规格：80mg\n用法：每日1次，每次1粒\n有效期：2025-08-31",
            "药品名称：格列齐特缓释片\n规格：30mg\n用法：每日1次，每次1片\n有效期：2026-04-30"
        };
        String[] reviewComments = {
            "OCR识别准确，药品信息完整。",
            "识别结果正确，建议确认患者肾功能。",
            "识别无误，注意药物过敏史。",
            "OCR识别清晰，用药剂量合理。",
            "识别结果准确，与医嘱一致。",
            "识别正确，注意心率监测。",
            "OCR识别完整，建议监测血压。",
            "识别无误，注意血糖监测。"
        };
        
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 0; i < 8; i++) {
            OcrReview review = new OcrReview();
            review.setUserId(userId);
            review.setImageUrl("/uploads/ocr/" + (i + 1) + ".jpg");
            review.setOcrResult(ocrResults[i]);
            review.setMedicationName(medicationNames[i]);
            review.setDosage(dosages[i]);
            review.setStatus(i % 3);
            
            if (i % 3 != 0) {
                review.setReviewTime(now.minusHours(i * 2));
                review.setReviewerId(1L);
                review.setReviewComment(reviewComments[i]);
            }
            
            review.setCreateTime(LocalDateTime.parse(now.minusDays(i / 2).minusHours(i * 3).format(formatter), formatter));
            
            repository.save(review);
        }
        System.out.println("生成了8条OCR审核测试数据");
    }

    private void generatePillReviewData(PillRecognitionReviewRepository repository, Long userId) {
        if (repository.count() > 0) {
            System.out.println("药片识别审核表已有数据，跳过生成");
            return;
        }
        
        String[] pillNames = {"硝苯地平控释片", "阿司匹林肠溶片", "二甲双胍缓释片", "阿托伐他汀钙片", "缬沙坦胶囊", "氯吡格雷片", "美托洛尔缓释片", "格列齐特缓释片", "瑞舒伐他汀钙片", "依折麦布片"};
        String[] pillDescriptions = {
            "红色圆形药片，刻有'NIF'字样，规格30mg",
            "白色肠溶片，圆形，规格100mg",
            "白色椭圆形药片，刻有'MET'字样，规格500mg",
            "白色圆形药片，刻有'ATOR'字样，规格20mg",
            "黄色胶囊，印有'VAL'字样，规格80mg",
            "粉红色圆形药片，刻有'CLP'字样，规格75mg",
            "白色圆形药片，刻有'MET'字样，规格47.5mg",
            "蓝色圆形药片，刻有'GLI'字样，规格30mg",
            "粉红色椭圆形药片，刻有'ROS'字样，规格10mg",
            "白色圆形药片，刻有'EZE'字样，规格10mg"
        };
        BigDecimal[] confidences = {
            new BigDecimal("0.98"), new BigDecimal("0.95"), new BigDecimal("0.97"),
            new BigDecimal("0.92"), new BigDecimal("0.99"), new BigDecimal("0.94"),
            new BigDecimal("0.96"), new BigDecimal("0.93"), new BigDecimal("0.91"), new BigDecimal("0.89")
        };
        String[] reviewComments = {
            "药片识别准确，置信度高。",
            "识别结果可靠，与处方一致。",
            "AI识别正确，建议确认患者用药史。",
            "识别结果合理，注意用药剂量。",
            "药片识别准确，信息完整。",
            "识别无误，建议监测血小板。",
            "识别结果正确，注意心率变化。",
            "药片识别清晰，建议血糖监测。",
            "识别合理，注意肝功能。",
            "识别结果准确，联合用药方案合理。"
        };
        
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 0; i < 10; i++) {
            PillRecognitionReview review = new PillRecognitionReview();
            review.setUserId(userId);
            review.setImageUrl("/uploads/pill/" + (i + 1) + ".jpg");
            review.setPillName(pillNames[i]);
            review.setPillDescription(pillDescriptions[i]);
            review.setConfidence(confidences[i]);
            review.setStatus(i % 3);
            
            if (i % 3 != 0) {
                review.setReviewTime(now.minusHours(i));
                review.setReviewerId(1L);
                review.setReviewComment(reviewComments[i]);
            }
            
            review.setCreateTime(LocalDateTime.parse(now.minusDays(i / 3).minusHours(i * 2).format(formatter), formatter));
            
            repository.save(review);
        }
        System.out.println("生成了10条药片识别审核测试数据");
    }
}