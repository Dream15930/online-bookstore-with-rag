package com.bookstore.service;

import com.bookstore.dto.LoginRequest;
import com.bookstore.dto.RegisterRequest;
import com.bookstore.entity.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    Map<String, Object> register(RegisterRequest req);
    Map<String, Object> login(LoginRequest req);
    Map<String, Object> adminLogin(LoginRequest req);
    User getById(Long id);
    List<User> listAll();
    void toggleStatus(Long id, String callerRole);
    void createAdmin(String username, String password, String callerRole);
    void updateAddress(Long userId, String address);
    void updateProfile(User user);
}
