package com.social.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.social.entity.User;
import com.social.mapper.UserMapper;
import com.social.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    // 直接在类中创建 PasswordEncoder
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User register(String username, String password) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("ROLE_USER");
        user.setAvatar("https://picsum.photos/200/200?random=" + System.currentTimeMillis());
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }

    public String login(String username, String password) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        User user = userMapper.selectOne(wrapper);

        if (user != null && passwordEncoder.matches(password, user.getPasswordHash())) {
            return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        }
        return null;
    }

    public User getById(Long id) {
        return userMapper.selectById(id);
    }
}