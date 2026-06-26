package com.medical.service;

import com.medical.entity.User;
import com.medical.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务层
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 查询所有用户
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * 根据ID查询用户
     */
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * 根据用户名查询用户
     */
    public Optional<User> findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    /**
     * 创建用户
     */
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUserName(user.getUserName())) {
            throw new RuntimeException("用户名已存在");
        }
        return userRepository.save(user);
    }

    /**
     * 更新用户
     */
    public User updateUser(Long userId, User userDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setRealName(userDetails.getRealName());
        user.setPhone(userDetails.getPhone());
        user.setGender(userDetails.getGender());
        user.setAge(userDetails.getAge());

        return userRepository.save(user);
    }

    /**
     * 删除用户
     */
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
