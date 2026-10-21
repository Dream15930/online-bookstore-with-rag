package com.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookstore.dto.LoginRequest;
import com.bookstore.dto.RegisterRequest;
import com.bookstore.entity.User;
import com.bookstore.exception.ErrorCode;
import com.bookstore.mapper.UserMapper;
import com.bookstore.service.UserService;
import com.bookstore.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Map<String, Object> register(RegisterRequest req) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getUsername, req.getUsername());
        if (userMapper.selectCount(qw) > 0) {
            throw ErrorCode.USERNAME_EXISTS.ex();
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole("member");
        user.setStatus(1);
        user.setAddress(req.getAddress());
        userMapper.insert(user);

        log.info("新用户注册: id={}, username={}", user.getId(), user.getUsername());

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        return result;
    }

    @Override
    public Map<String, Object> login(LoginRequest req) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getUsername, req.getUsername());
        User user = userMapper.selectOne(qw);
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw ErrorCode.PASSWORD_ERROR.ex();
        }
        if (user.getStatus() == 0) {
            throw ErrorCode.ACCOUNT_DISABLED.ex();
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        return result;
    }

    @Override
    public Map<String, Object> adminLogin(LoginRequest req) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getUsername, req.getUsername());
        User user = userMapper.selectOne(qw);
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw ErrorCode.PASSWORD_ERROR.ex();
        }
        if (!"admin".equals(user.getRole()) && !"super_admin".equals(user.getRole())) {
            throw ErrorCode.NOT_ADMIN.ex();
        }
        if (user.getStatus() == 0) {
            throw ErrorCode.ACCOUNT_DISABLED.ex();
        }
        log.info("管理员登录: username={}, role={}", user.getUsername(), user.getRole());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        return result;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public List<User> listAll() {
        return userMapper.selectList(null);
    }

    @Override
    public void toggleStatus(Long id, String callerRole) {
        User user = userMapper.selectById(id);
        if (user == null) return;
        if (!"super_admin".equals(callerRole) && ("admin".equals(user.getRole()) || "super_admin".equals(user.getRole()))) {
            throw ErrorCode.NOT_SUPER_ADMIN.ex();
        }
        if ("super_admin".equals(user.getRole())) {
            throw ErrorCode.NOT_SUPER_ADMIN.ex();
        }
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userMapper.updateById(user);
        log.info("管理员 {} 修改用户 {} 状态为 {}", callerRole, user.getUsername(), user.getStatus());
    }

    @Override
    public void createAdmin(String username, String password, String callerRole) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getUsername, username);
        if (userMapper.selectCount(qw) > 0) {
            throw ErrorCode.USERNAME_EXISTS.ex();
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("admin");
        user.setStatus(1);
        userMapper.insert(user);
        log.info("管理员 {} 创建了新的管理员账号: {}", callerRole, username);
    }

    @Override
    public void updateAddress(Long userId, String address) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setAddress(address);
            userMapper.updateById(user);
        }
    }

    @Override
    public void updateProfile(User user) {
        userMapper.updateById(user);
    }
}
