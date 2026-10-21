package com.bookstore.controller;

import com.bookstore.entity.Address;
import com.bookstore.entity.User;
import com.bookstore.service.AddressService;
import com.bookstore.service.UserService;
import com.bookstore.util.Result;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final AddressService addressService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, AddressService addressService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.addressService = addressService;
        this.passwordEncoder = passwordEncoder;
    }

    private Long uid(Authentication auth) { return Long.valueOf(auth.getName()); }

    @GetMapping("/profile")
    public Result<User> profile(Authentication auth) {
        User user = userService.getById(uid(auth));
        user.setPassword(null);
        return Result.ok(user);
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(Authentication auth, @RequestBody Map<String, String> body) {
        User user = userService.getById(uid(auth));
        if (body.containsKey("username")) user.setUsername(body.get("username"));
        if (body.containsKey("password") && body.get("password") != null && !body.get("password").isBlank()) {
            user.setPassword(passwordEncoder.encode(body.get("password")));
        }
        userService.updateProfile(user);
        return Result.ok();
    }

    @GetMapping("/addresses")
    public Result<List<Address>> addresses(Authentication auth) {
        return Result.ok(addressService.listByUser(uid(auth)));
    }

    @PostMapping("/addresses")
    public Result<Void> addAddress(Authentication auth, @RequestBody Map<String, String> body) {
        addressService.add(uid(auth), body.get("address"));
        return Result.ok();
    }

    @PutMapping("/addresses/{id}")
    public Result<Void> updateAddress(Authentication auth, @PathVariable Long id, @RequestBody Map<String, String> body) {
        addressService.update(id, uid(auth), body.get("address"));
        return Result.ok();
    }

    @DeleteMapping("/addresses/{id}")
    public Result<Void> deleteAddress(Authentication auth, @PathVariable Long id) {
        addressService.delete(id, uid(auth));
        return Result.ok();
    }

    @PutMapping("/addresses/{id}/default")
    public Result<Void> setDefaultAddress(Authentication auth, @PathVariable Long id) {
        addressService.setDefault(id, uid(auth));
        return Result.ok();
    }
}
