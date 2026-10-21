package com.bookstore.controller;

import com.bookstore.dto.LoginRequest;
import com.bookstore.dto.RegisterRequest;
import com.bookstore.entity.User;
import com.bookstore.service.UserService;
import com.bookstore.util.Result;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        return Result.ok(userService.register(req));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(userService.login(req));
    }

    @PostMapping("/admin/login")
    public Result<Map<String, Object>> adminLogin(@Valid @RequestBody LoginRequest req) {
        return Result.ok(userService.adminLogin(req));
    }

    @GetMapping("/me")
    public Result<User> me(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        User user = userService.getById(userId);
        user.setPassword(null);
        return Result.ok(user);
    }
}
