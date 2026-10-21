package com.bookstore.controller;

import com.bookstore.dto.CartVO;
import com.bookstore.service.CartService;
import com.bookstore.util.Result;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Result<List<CartVO>> list(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return Result.ok(cartService.list(userId));
    }

    @PostMapping
    public Result<Void> add(Authentication auth, @RequestBody Map<String, Long> body) {
        Long userId = Long.valueOf(auth.getName());
        cartService.add(userId, body.get("bookId"));
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(Authentication auth, @PathVariable Long id,
                                @RequestBody Map<String, Integer> body) {
        Long userId = Long.valueOf(auth.getName());
        cartService.updateQuantity(id, userId, body.get("quantity"));
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(Authentication auth, @PathVariable Long id) {
        Long userId = Long.valueOf(auth.getName());
        cartService.delete(id, userId);
        return Result.ok();
    }
}
