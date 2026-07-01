package com.medical.controller;

import com.medical.dto.Result;
import com.medical.entity.ApiConfig;
import com.medical.entity.SystemAnnouncement;
import com.medical.entity.User;
import com.medical.repository.ApiConfigRepository;
import com.medical.repository.SystemAnnouncementRepository;
import com.medical.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/system")
public class SystemCenterController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SystemAnnouncementRepository announcementRepository;
    
    @Autowired
    private ApiConfigRepository apiConfigRepository;

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        long userCount = userRepository.count();
        statistics.put("userCount", userCount);
        
        long adminCount = 0;
        statistics.put("adminCount", adminCount);
        
        long announcementCount = announcementRepository.count();
        statistics.put("announcementCount", announcementCount);
        
        long activeConfigCount = apiConfigRepository.findByStatus(1).size();
        statistics.put("activeConfigCount", activeConfigCount);
        
        return Result.success("查询成功", statistics);
    }

    @GetMapping("/announcements")
    public Result<Map<String, Object>> getAnnouncementList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "create_time"));
        Page<SystemAnnouncement> page;
        
        if (status != null) {
            page = announcementRepository.findByKeywordAndStatus(keyword, status, pageable);
        } else {
            page = announcementRepository.findByKeywordOnly(keyword, pageable);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getContent());
        data.put("total", page.getTotalElements());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        
        return Result.success("查询成功", data);
    }

    @GetMapping("/announcements/{announceId}")
    public Result<SystemAnnouncement> getAnnouncementDetail(@PathVariable Long announceId) {
        return announcementRepository.findById(announceId)
                .map(Result::success)
                .orElse(Result.error("公告不存在"));
    }

    @PostMapping("/announcements")
    public Result<SystemAnnouncement> createAnnouncement(@RequestBody SystemAnnouncement announcement) {
        announcement.setStatus(1);
        announcement.setPublishTime(LocalDateTime.now());
        SystemAnnouncement saved = announcementRepository.save(announcement);
        return Result.success("创建成功", saved);
    }

    @PutMapping("/announcements/{announceId}")
    public Result<SystemAnnouncement> updateAnnouncement(@PathVariable Long announceId, @RequestBody SystemAnnouncement announcement) {
        return announcementRepository.findById(announceId)
                .map(existing -> {
                    existing.setTitle(announcement.getTitle());
                    existing.setContent(announcement.getContent());
                    existing.setType(announcement.getType());
                    existing.setTarget(announcement.getTarget());
                    existing.setEndTime(announcement.getEndTime());
                    existing.setStatus(announcement.getStatus());
                    SystemAnnouncement updated = announcementRepository.save(existing);
                    return Result.success("更新成功", updated);
                })
                .orElse(Result.error("公告不存在"));
    }

    @DeleteMapping("/announcements/{announceId}")
    public Result<String> deleteAnnouncement(@PathVariable Long announceId) {
        if (!announcementRepository.existsById(announceId)) {
            return Result.error("公告不存在");
        }
        announcementRepository.deleteById(announceId);
        return Result.success("删除成功");
    }

    @GetMapping("/ai-configs")
    public Result<Map<String, Object>> getAiConfigList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "update_time"));
        Page<ApiConfig> page = apiConfigRepository.findByKeyword(keyword, pageable);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getContent());
        data.put("total", page.getTotalElements());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        
        return Result.success("查询成功", data);
    }

    @GetMapping("/ai-configs/{configId}")
    public Result<ApiConfig> getAiConfigDetail(@PathVariable Long configId) {
        return apiConfigRepository.findById(configId)
                .map(Result::success)
                .orElse(Result.error("配置不存在"));
    }

    @PostMapping("/ai-configs")
    public Result<ApiConfig> createAiConfig(@RequestBody ApiConfig config) {
        config.setStatus(1);
        ApiConfig saved = apiConfigRepository.save(config);
        return Result.success("创建成功", saved);
    }

    @PutMapping("/ai-configs/{configId}")
    public Result<ApiConfig> updateAiConfig(@PathVariable Long configId, @RequestBody ApiConfig config) {
        return apiConfigRepository.findById(configId)
                .map(existing -> {
                    existing.setApiName(config.getApiName());
                    existing.setApiType(config.getApiType());
                    existing.setEndpoint(config.getEndpoint());
                    existing.setAppKey(config.getAppKey());
                    existing.setAppSecret(config.getAppSecret());
                    existing.setStatus(config.getStatus());
                    existing.setRemarks(config.getRemarks());
                    ApiConfig updated = apiConfigRepository.save(existing);
                    return Result.success("更新成功", updated);
                })
                .orElse(Result.error("配置不存在"));
    }

    @DeleteMapping("/ai-configs/{configId}")
    public Result<String> deleteAiConfig(@PathVariable Long configId) {
        if (!apiConfigRepository.existsById(configId)) {
            return Result.error("配置不存在");
        }
        apiConfigRepository.deleteById(configId);
        return Result.success("删除成功");
    }

    @PutMapping("/ai-configs/{configId}/toggle")
    public Result<ApiConfig> toggleAiConfigStatus(@PathVariable Long configId) {
        return apiConfigRepository.findById(configId)
                .map(config -> {
                    config.setStatus(config.getStatus() == 1 ? 0 : 1);
                    ApiConfig updated = apiConfigRepository.save(config);
                    return Result.success("状态切换成功", updated);
                })
                .orElse(Result.error("配置不存在"));
    }
}