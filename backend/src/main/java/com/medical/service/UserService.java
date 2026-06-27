package com.medical.service;

import com.medical.entity.User;
import com.medical.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务层
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 创建用户
     */
    public User createUser(User user) {
        if (userRepository.existsByUserName(user.getUserName())) {
            throw new RuntimeException("用户名已存在");
        }
        return userRepository.save(user);
    }
    
    /**
     * 根据ID查询用户
     */
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }
    
    /**
     * 根据用户名查询用户
     */
    public Optional<User> getUserByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }
    
    /**
     * 查询所有用户
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * 更新用户
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    /**
     * 删除用户
     */
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
    
    /**
     * 用户登录验证
     */
    public User login(String userName, String password) {
        Optional<User> userOpt = userRepository.findByUserName(userName);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
}