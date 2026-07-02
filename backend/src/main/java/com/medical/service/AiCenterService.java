package com.medical.service;

import com.medical.entity.AiMedicationPlan;
import com.medical.entity.OcrReview;
import com.medical.entity.PillRecognitionReview;
import com.medical.repository.AiMedicationPlanRepository;
import com.medical.repository.OcrReviewRepository;
import com.medical.repository.PillRecognitionReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AiCenterService {

    @Autowired
    private AiMedicationPlanRepository medicationPlanRepository;

    @Autowired
    private OcrReviewRepository ocrReviewRepository;

    @Autowired
    private PillRecognitionReviewRepository pillRecognitionReviewRepository;

    public Page<AiMedicationPlan> getMedicationPlanPage(int pageNum, int pageSize, String keyword, Integer status) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "create_time"));
        return medicationPlanRepository.findByKeywordAndStatus(keyword, status, pageable);
    }

    public AiMedicationPlan getMedicationPlanById(Long id) {
        return medicationPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用药计划不存在"));
    }

    public AiMedicationPlan createMedicationPlan(AiMedicationPlan plan) {
        plan.setStatus(1);
        return medicationPlanRepository.save(plan);
    }

    public AiMedicationPlan updateMedicationPlan(Long id, AiMedicationPlan plan) {
        AiMedicationPlan existing = medicationPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用药计划不存在"));
        if (plan.getPlanName() != null) existing.setPlanName(plan.getPlanName());
        if (plan.getDiseaseType() != null) existing.setDiseaseType(plan.getDiseaseType());
        if (plan.getMedicationList() != null) existing.setMedicationList(plan.getMedicationList());
        if (plan.getDosageInstructions() != null) existing.setDosageInstructions(plan.getDosageInstructions());
        if (plan.getStartDate() != null) existing.setStartDate(plan.getStartDate());
        if (plan.getEndDate() != null) existing.setEndDate(plan.getEndDate());
        if (plan.getStatus() != null) existing.setStatus(plan.getStatus());
        if (plan.getAiAnalysis() != null) existing.setAiAnalysis(plan.getAiAnalysis());
        return medicationPlanRepository.save(existing);
    }

    public void deleteMedicationPlan(Long id) {
        if (!medicationPlanRepository.existsById(id)) {
            throw new RuntimeException("用药计划不存在");
        }
        medicationPlanRepository.deleteById(id);
    }

    public Page<OcrReview> getOcrReviewPage(int pageNum, int pageSize, String keyword, Integer status) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "create_time"));
        return ocrReviewRepository.findByKeywordAndStatus(keyword, status, pageable);
    }

    public OcrReview getOcrReviewById(Long id) {
        return ocrReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OCR审核记录不存在"));
    }

    public OcrReview reviewOcr(Long id, Integer status, Long reviewerId, String comment) {
        OcrReview ocr = ocrReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OCR审核记录不存在"));
        ocr.setStatus(status);
        ocr.setReviewTime(LocalDateTime.now());
        ocr.setReviewerId(reviewerId);
        ocr.setReviewComment(comment);
        return ocrReviewRepository.save(ocr);
    }

    public void deleteOcrReview(Long id) {
        if (!ocrReviewRepository.existsById(id)) {
            throw new RuntimeException("OCR审核记录不存在");
        }
        ocrReviewRepository.deleteById(id);
    }

    public Page<PillRecognitionReview> getPillReviewPage(int pageNum, int pageSize, String keyword, Integer status) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "create_time"));
        return pillRecognitionReviewRepository.findByKeywordAndStatus(keyword, status, pageable);
    }

    public PillRecognitionReview getPillReviewById(Long id) {
        return pillRecognitionReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("药片识别审核记录不存在"));
    }

    public PillRecognitionReview reviewPill(Long id, Integer status, Long reviewerId, String comment) {
        PillRecognitionReview pill = pillRecognitionReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("药片识别审核记录不存在"));
        pill.setStatus(status);
        pill.setReviewTime(LocalDateTime.now());
        pill.setReviewerId(reviewerId);
        pill.setReviewComment(comment);
        return pillRecognitionReviewRepository.save(pill);
    }

    public void deletePillReview(Long id) {
        if (!pillRecognitionReviewRepository.existsById(id)) {
            throw new RuntimeException("药片识别审核记录不存在");
        }
        pillRecognitionReviewRepository.deleteById(id);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("planCount", medicationPlanRepository.count());
        stats.put("ocrPendingCount", ocrReviewRepository.findByStatusOrderByCreateTimeDesc(0).size());
        stats.put("ocrApprovedCount", ocrReviewRepository.findByStatusOrderByCreateTimeDesc(1).size());
        stats.put("ocrRejectedCount", ocrReviewRepository.findByStatusOrderByCreateTimeDesc(2).size());
        stats.put("pillPendingCount", pillRecognitionReviewRepository.findByStatusOrderByCreateTimeDesc(0).size());
        stats.put("pillApprovedCount", pillRecognitionReviewRepository.findByStatusOrderByCreateTimeDesc(1).size());
        stats.put("pillRejectedCount", pillRecognitionReviewRepository.findByStatusOrderByCreateTimeDesc(2).size());
        return stats;
    }
}