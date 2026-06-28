package com.medical.config;

import com.medical.entity.Admin;
import com.medical.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化类
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!adminRepository.existsByAdminName("admin")) {
            Admin admin = new Admin();
            admin.setAdminName("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRealname("Administrator");
            admin.setPhone("13800138000");
            adminRepository.save(admin);
            System.out.println("==========================================");
            System.out.println("  默认管理员账号已创建: admin / admin123");
            System.out.println("==========================================");
        } else {
            Admin admin = adminRepository.findByAdminName("admin").orElse(null);
            if (admin != null && !admin.getPassword().startsWith("$2a$")) {
                admin.setPassword(passwordEncoder.encode("admin123"));
                adminRepository.save(admin);
                System.out.println("==========================================");
                System.out.println("  管理员密码已更新为加密格式");
                System.out.println("==========================================");
            }
        }
    }
}