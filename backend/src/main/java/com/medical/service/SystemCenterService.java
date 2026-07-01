package com.medical.service;

import com.medical.entity.ApiConfig;
import com.medical.entity.SystemAnnouncement;
import com.medical.repository.ApiConfigRepository;
import com.medical.repository.SystemAnnouncementRepository;
import com.medical.repository.UserRepository;
import com.medical.repository.MedicationPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SystemCenterService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicationPlanRepository medicationPlanRepository;

    @Autowired
    private SystemAnnouncementRepository announcementRepository;

    @Autowired
    private ApiConfigRepository apiConfigRepository;

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = userRepository.count();
        long totalPlans = medicationPlanRepository.count();

        stats.put("totalUsers", totalUsers);
        stats.put("totalPlans", totalPlans);
        stats.put("totalReminders", 0);
        stats.put("totalFamilyMembers", 0);
        stats.put("sentReminders", 0);
        stats.put("receivedReminders", 0);
        stats.put("receiveRate", 0);

        return stats;
    }

    public Page<SystemAnnouncement> getAnnouncementPage(int pageNum, int pageSize, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "create_time"));
        return announcementRepository.findByKeyword(keyword, pageable);
    }

    public List<SystemAnnouncement> getPublishedAnnouncements() {
        return announcementRepository.findByStatusOrderByPublishTimeDesc(1);
    }

    public SystemAnnouncement getAnnouncementById(Long announceId) {
        return announcementRepository.findById(announceId)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
    }

    public SystemAnnouncement createAnnouncement(SystemAnnouncement announcement) {
        return announcementRepository.save(announcement);
    }

    public SystemAnnouncement updateAnnouncement(Long announceId, SystemAnnouncement announcement) {
        SystemAnnouncement existing = announcementRepository.findById(announceId)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
        
        if (announcement.getTitle() != null) existing.setTitle(announcement.getTitle());
        if (announcement.getContent() != null) existing.setContent(announcement.getContent());
        if (announcement.getType() != null) existing.setType(announcement.getType());
        if (announcement.getTarget() != null) existing.setTarget(announcement.getTarget());
        if (announcement.getPublishTime() != null) existing.setPublishTime(announcement.getPublishTime());
        if (announcement.getEndTime() != null) existing.setEndTime(announcement.getEndTime());
        if (announcement.getStatus() != null) existing.setStatus(announcement.getStatus());
        
        return announcementRepository.save(existing);
    }

    public void deleteAnnouncement(Long announceId) {
        if (!announcementRepository.existsById(announceId)) {
            throw new RuntimeException("公告不存在");
        }
        announcementRepository.deleteById(announceId);
    }

    public Page<ApiConfig> getApiConfigPage(int pageNum, int pageSize, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "create_time"));
        return apiConfigRepository.findByKeyword(keyword, pageable);
    }

    public List<ApiConfig> getApiConfigsByType(String apiType) {
        return apiConfigRepository.findByApiType(apiType);
    }

    public ApiConfig getApiConfigById(Long configId) {
        return apiConfigRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("配置不存在"));
    }

    public ApiConfig createApiConfig(ApiConfig config) {
        return apiConfigRepository.save(config);
    }

    public ApiConfig updateApiConfig(Long configId, ApiConfig config) {
        ApiConfig existing = apiConfigRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("配置不存在"));
        
        if (config.getApiName() != null) existing.setApiName(config.getApiName());
        if (config.getApiType() != null) existing.setApiType(config.getApiType());
        if (config.getEndpoint() != null) existing.setEndpoint(config.getEndpoint());
        if (config.getAppKey() != null) existing.setAppKey(config.getAppKey());
        if (config.getAppSecret() != null) existing.setAppSecret(config.getAppSecret());
        if (config.getStatus() != null) existing.setStatus(config.getStatus());
        if (config.getRemarks() != null) existing.setRemarks(config.getRemarks());
        
        return apiConfigRepository.save(existing);
    }

    public void deleteApiConfig(Long configId) {
        if (!apiConfigRepository.existsById(configId)) {
            throw new RuntimeException("配置不存在");
        }
        apiConfigRepository.deleteById(configId);
    }
}