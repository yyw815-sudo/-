package com.medical.service;

import com.medical.entity.Admin;
import com.medical.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdminService {
    
    @Autowired
    private AdminRepository adminRepository;
    
    public Admin createAdmin(Admin admin) {
        if (adminRepository.existsByAdminName(admin.getAdminName())) {
            throw new RuntimeException("管理员用户名已存在");
        }
        return adminRepository.save(admin);
    }
    
    public Optional<Admin> getAdminById(Long adminId) {
        return adminRepository.findById(adminId);
    }
    
    public Optional<Admin> getAdminByAdminName(String adminName) {
        return adminRepository.findByAdminName(adminName);
    }
    
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }
    
    public Admin updateAdmin(Admin admin) {
        return adminRepository.save(admin);
    }
    
    public void deleteAdmin(Long adminId) {
        adminRepository.deleteById(adminId);
    }
    
    public Admin login(String adminName, String password) {
        Optional<Admin> adminOpt = adminRepository.findByAdminName(adminName);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (password.equals(admin.getPassword())) {
                return admin;
            }
        }
        return null;
    }

    public Page<Admin> getAdminPage(int pageNum, int pageSize, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "create_time"));
        return adminRepository.findAll(pageable);
    }

    public void resetPassword(Long adminId, String newPassword) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        admin.setPassword(newPassword);
        adminRepository.save(admin);
    }
}