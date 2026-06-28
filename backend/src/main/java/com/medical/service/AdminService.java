package com.medical.service;

import com.medical.entity.Admin;
import com.medical.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * 管理员服务层
 */
@Service
@Transactional
public class AdminService {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 创建管理员
     */
    public Admin createAdmin(Admin admin) {
        if (adminRepository.existsByAdminName(admin.getAdminName())) {
            throw new RuntimeException("管理员用户名已存在");
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return adminRepository.save(admin);
    }
    
    /**
     * 根据ID查询管理员
     */
    public Optional<Admin> getAdminById(Long adminId) {
        return adminRepository.findById(adminId);
    }
    
    /**
     * 根据用户名查询管理员
     */
    public Optional<Admin> getAdminByAdminName(String adminName) {
        return adminRepository.findByAdminName(adminName);
    }
    
    /**
     * 查询所有管理员
     */
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }
    
    /**
     * 更新管理员
     */
    public Admin updateAdmin(Admin admin) {
        return adminRepository.save(admin);
    }
    
    /**
     * 删除管理员
     */
    public void deleteAdmin(Long adminId) {
        adminRepository.deleteById(adminId);
    }
    
    /**
     * 管理员登录验证
     */
    public Admin login(String adminName, String password) {
        Optional<Admin> adminOpt = adminRepository.findByAdminName(adminName);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (passwordEncoder.matches(password, admin.getPassword())) {
                return admin;
            }
        }
        return null;
    }
}